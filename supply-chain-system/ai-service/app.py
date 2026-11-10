"""
AI 智能分析服务
- 销量预测：基于历史销量的移动平均+趋势外推（简化版 Prophet 思路）
- 智能补货：结合预测销量、当前库存、安全库存计算最优补货量
- 异常检测：基于 Isolation Forest（孤立森林）识别库存/订单/采购异常
"""
import os
import logging
from datetime import datetime, date, timedelta
from typing import List, Optional, Dict, Any

import numpy as np
import pandas as pd
from fastapi import FastAPI, Query, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from sqlalchemy import create_engine, text
from sklearn.ensemble import IsolationForest

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
log = logging.getLogger("ai-service")

DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = os.getenv("DB_PORT", "3306")
DB_USER = os.getenv("DB_USER", "scuser")
DB_PASSWORD = os.getenv("DB_PASSWORD", "sc123456")
DB_NAME = os.getenv("DB_NAME", "supply_chain")

DATABASE_URL = f"mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}?charset=utf8mb4"
engine = create_engine(DATABASE_URL, pool_pre_ping=True, pool_recycle=3600)

app = FastAPI(title="Supply Chain AI Service", version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# ---------- Pydantic Schemas ----------
class ReplenishmentVO(BaseModel):
    id: int
    productId: int
    productName: Optional[str] = None
    productCode: Optional[str] = None
    currentStock: int
    predictedSales: int
    safetyStock: int
    suggestedQty: int
    status: int
    createTime: Optional[str] = None


class PredictionVO(BaseModel):
    id: int
    productId: int
    productName: Optional[str] = None
    predictDate: str
    predictQuantity: int
    modelType: str
    confidence: Optional[float] = None


class AnomalyVO(BaseModel):
    id: int
    alertType: str
    productId: Optional[int] = None
    productName: Optional[str] = None
    refOrderNo: Optional[str] = None
    anomalyScore: float
    description: str
    status: int
    createTime: Optional[str] = None


# ---------- Helpers ----------
def query_to_dicts(sql: str, params: dict = None) -> List[Dict[str, Any]]:
    with engine.connect() as conn:
        result = conn.execute(text(sql), params or {})
        rows = [dict(r._mapping) for r in result.fetchall()]
    return rows


def execute_sql(sql: str, params: dict = None):
    with engine.begin() as conn:
        conn.execute(text(sql), params or {})


def get_product_map() -> Dict[int, Dict]:
    rows = query_to_dicts("SELECT id, product_code, product_name FROM product WHERE deleted=0")
    return {r["id"]: {"code": r["product_code"], "name": r["product_name"]} for r in rows}


# ---------- 销量预测 ----------
def fetch_sales_history(days: int = 90) -> pd.DataFrame:
    """拉取订单明细聚合的按日按商品销量。若历史不足则使用库存流水SALE出库做补充。"""
    start = (datetime.now() - timedelta(days=days)).strftime("%Y-%m-%d")
    sql = """
    SELECT DATE(o.create_time) AS sale_date, oi.product_id AS product_id, SUM(oi.quantity) AS qty
    FROM order_item oi
    JOIN orders o ON o.id = oi.order_id
    WHERE o.status IN ('COMPLETED','SHIPPED') AND DATE(o.create_time) >= :start
    GROUP BY DATE(o.create_time), oi.product_id
    """
    rows = query_to_dicts(sql, {"start": start})
    df = pd.DataFrame(rows)
    if df.empty:
        # fallback: 使用 inventory_record 的 SALE OUT
        sql2 = """
        SELECT DATE(create_time) AS sale_date, product_id, SUM(quantity) AS qty
        FROM inventory_record
        WHERE biz_type='SALE' AND type='OUT' AND DATE(create_time) >= :start
        GROUP BY DATE(create_time), product_id
        """
        rows = query_to_dicts(sql2, {"start": start})
        df = pd.DataFrame(rows)
    if df.empty:
        return df
    df["sale_date"] = pd.to_datetime(df["sale_date"])
    df["qty"] = df["qty"].astype(int)
    return df


def predict_series(series: pd.Series, horizon: int = 7, base_default: int = 10) -> np.ndarray:
    """
    简单的时序预测：加权移动平均 + 线性趋势。
    - 取最近有销量的实际数据计算平均值（忽略大量0填充的无销量日）
    - 计算趋势
    - 综合得到未来 horizon 天预测值
    """
    s = series.tail(60).reset_index(drop=True).astype(float)
    non_zero = s[s > 0]
    if len(non_zero) == 0:
        return np.array([max(base_default, 1)] * horizon, dtype=int)
    avg_sales = non_zero.mean()
    if len(s) >= 14:
        recent_nonzero = s.tail(14)
        recent_nonzero = recent_nonzero[recent_nonzero > 0]
        recent_avg = recent_nonzero.mean() if len(recent_nonzero) > 0 else avg_sales
        prev_nonzero = s.iloc[-28:-14] if len(s) >= 28 else s.iloc[:-14]
        prev_nonzero = prev_nonzero[prev_nonzero > 0]
        prev_avg = prev_nonzero.mean() if len(prev_nonzero) > 0 else avg_sales
        trend = (recent_avg - prev_avg) / 7.0 if prev_avg > 0 else 0.0
    else:
        recent_avg = avg_sales
        trend = 0.0
    base = max(recent_avg, avg_sales * 0.8, 1)
    preds = []
    for h in range(1, horizon + 1):
        v = base + trend * h
        preds.append(max(int(round(v)), 1))
    return np.array(preds)


@app.post("/api/v1/ai/prediction/run", summary="手动触发销量预测任务")
def run_prediction(horizon: int = Query(7, ge=1, le=30)):
    log.info("开始执行销量预测任务，预测未来 %s 天", horizon)
    execute_sql("DELETE FROM sales_prediction WHERE model_type='MOVING_AVG' AND predict_date >= CURDATE()")
    df = fetch_sales_history(120)
    products = get_product_map()
    today = date.today()
    insert_sql = """
    INSERT INTO sales_prediction (product_id, predict_date, predict_quantity, model_type, confidence, create_time)
    VALUES (:pid, :pd, :pq, :mt, :cf, NOW())
    """
    pred_count = 0
    params_list = []
    inv_map = {r["product_id"]: int(r["safety_stock"]) for r in query_to_dicts("SELECT product_id, safety_stock FROM inventory")}
    for pid, pinfo in products.items():
        safety = inv_map.get(pid, 10)
        if not df.empty and pid in df["product_id"].values:
            s = df[df["product_id"] == pid].sort_values("sale_date").set_index("sale_date")["qty"]
            if len(s) > 0:
                idx = pd.date_range(end=today - timedelta(days=1), periods=max(30, len(s)), freq="D")
                s = s.reindex(idx, fill_value=0)
            preds = predict_series(s, horizon, base_default=max(safety, 5))
            confidence = round(min(0.95, 0.6 + 0.03 * min(len(s[s > 0]), 12)), 2)
        else:
            base = max(safety, 5)
            preds = np.array([max(int(base * 0.8), 1)] * horizon)
            confidence = 0.5
        for i, q in enumerate(preds):
            pd_date = today + timedelta(days=i + 1)
            q_val = max(int(q), 1)
            params_list.append({
                "pid": pid, "pd": pd_date.strftime("%Y-%m-%d"),
                "pq": q_val, "mt": "MOVING_AVG", "cf": float(confidence)
            })
            pred_count += 1
    with engine.begin() as conn:
        for p in params_list:
            conn.execute(text(insert_sql), p)
    log.info("预测完成，写入 %d 条预测记录", pred_count)
    generate_replenishment_suggestions()
    return {"code": 200, "message": f"预测完成，生成{pred_count}条记录", "data": {"count": pred_count}}


@app.get("/api/v1/ai/prediction", response_model=Dict[str, Any])
def get_predictions(productId: Optional[int] = Query(None),
                    days: int = Query(7, ge=1, le=30),
                    pageNum: int = 1, pageSize: int = 20):
    where = "WHERE sp.predict_date >= CURDATE() AND sp.predict_date < DATE_ADD(CURDATE(), INTERVAL :days DAY)"
    params = {"days": days}
    if productId:
        where += " AND sp.product_id = :pid"
        params["pid"] = productId
    total = query_to_dicts(f"SELECT COUNT(*) c FROM sales_prediction sp {where}", params)[0]["c"]
    if total == 0:
        try:
            run_prediction(days)
            total = query_to_dicts(f"SELECT COUNT(*) c FROM sales_prediction sp {where}", params)[0]["c"]
        except Exception as e:
            log.warning("自动预测失败: %s", e)
    sql = f"""
    SELECT sp.*, p.product_name FROM sales_prediction sp
    LEFT JOIN product p ON p.id = sp.product_id
    {where}
    ORDER BY sp.product_id, sp.predict_date
    LIMIT :offset,:limit
    """
    params.update({"offset": (pageNum - 1) * pageSize, "limit": pageSize})
    rows = query_to_dicts(sql, params)
    data = [{
        "id": r["id"], "productId": r["product_id"], "productName": r["product_name"],
        "predictDate": r["predict_date"].strftime("%Y-%m-%d") if hasattr(r["predict_date"], "strftime") else str(r["predict_date"]),
        "predictQuantity": max(int(r["predict_quantity"]), 1),
        "predictedQty": max(int(r["predict_quantity"]), 1),
        "predictSales": max(int(r["predict_quantity"]), 1),
        "modelType": r["model_type"], "model": r["model_type"], "modelName": r["model_type"],
        "confidence": float(r["confidence"]) if r["confidence"] else None
    } for r in rows]
    return {"code": 200, "message": "success", "data": {"total": total, "list": data}}


# ---------- 智能补货 ----------
def generate_replenishment_suggestions():
    log.info("生成智能补货建议")
    execute_sql("DELETE FROM replenishment_suggestion WHERE status=0")
    # 取未来7天预测汇总
    sql_pred = """
    SELECT product_id, SUM(predict_quantity) AS pred_7d
    FROM sales_prediction
    WHERE predict_date >= CURDATE() AND predict_date < DATE_ADD(CURDATE(), INTERVAL 7 DAY)
    GROUP BY product_id
    """
    preds = {r["product_id"]: int(r["pred_7d"]) for r in query_to_dicts(sql_pred)}
    invs = query_to_dicts("SELECT product_id, quantity, safety_stock FROM inventory")
    insert_sql = """
    INSERT INTO replenishment_suggestion (product_id, current_stock, predicted_sales, safety_stock, suggested_qty, status, create_time)
    VALUES (:pid, :cs, :ps, :ss, :sq, 0, NOW())
    """
    count = 0
    with engine.begin() as conn:
        for inv in invs:
            pid = inv["product_id"]
            current = int(inv["quantity"])
            safety = int(inv["safety_stock"])
            pred = preds.get(pid, int(safety * 2))
            suggested = max(pred + safety - current, 0)
            # 低于安全库存或预测需求大于当前库存才建议补货
            if current < safety or suggested > 0:
                conn.execute(text(insert_sql), {"pid": pid, "cs": current, "ps": pred, "ss": safety, "sq": suggested})
                count += 1
    log.info("补货建议生成: %d 条", count)
    return count


@app.get("/api/v1/ai/replenishment", response_model=Dict[str, Any])
def list_replenishment(pageNum: int = 1, pageSize: int = 20, status: Optional[int] = None):
    where = "WHERE 1=1"
    params = {}
    if status is not None:
        where += " AND rs.status=:st"
        params["st"] = status
    total = query_to_dicts(f"SELECT COUNT(*) c FROM replenishment_suggestion rs {where}", params)[0]["c"]
    sql = f"""
    SELECT rs.*, p.product_name, p.product_code FROM replenishment_suggestion rs
    LEFT JOIN product p ON p.id = rs.product_id
    {where}
    ORDER BY rs.suggested_qty DESC
    LIMIT :offset,:limit
    """
    params.update({"offset": (pageNum - 1) * pageSize, "limit": pageSize})
    rows = query_to_dicts(sql, params)
    data = [{
        "id": r["id"], "productId": r["product_id"],
        "productName": r["product_name"], "productCode": r["product_code"],
        "currentStock": int(r["current_stock"]), "predictedSales": int(r["predicted_sales"]),
        "predictedQty": int(r["predicted_sales"]), "predict7dSales": int(r["predicted_sales"]),
        "forecast7dQty": int(r["predicted_sales"]),
        "safetyStock": int(r["safety_stock"]), "suggestedQty": int(r["suggested_qty"]),
        "suggestQty": int(r["suggested_qty"]), "replenishQty": int(r["suggested_qty"]),
        "status": "PENDING" if int(r["status"]) == 0 else "HANDLED",
        "statusCode": int(r["status"]),
        "createTime": r["create_time"].strftime("%Y-%m-%d %H:%M:%S") if r["create_time"] else None
    } for r in rows]
    return {"code": 200, "message": "success", "data": {"total": total, "list": data}}


@app.post("/api/v1/ai/replenishment/convert", response_model=Dict[str, Any])
def convert_to_purchase():
    """将未处理补货建议批量转为采购单（调用 purchase-service 时需要HTTP，简单做：标记为已处理并返回建议数据）"""
    rows = query_to_dicts("SELECT * FROM replenishment_suggestion WHERE status=0 AND suggested_qty>0")
    if not rows:
        return {"code": 200, "message": "没有待处理的补货建议", "data": {"count": 0}}
    execute_sql("UPDATE replenishment_suggestion SET status=1 WHERE status=0 AND suggested_qty>0")
    return {"code": 200, "message": "已标记为已生成采购单（请在采购管理中手动创建，共{}条建议）".format(len(rows)),
            "data": {"count": len(rows)}}


# ---------- 异常检测（Isolation Forest） ----------
@app.post("/api/v1/ai/anomaly/detect", summary="手动执行异常检测")
def detect_anomaly():
    log.info("开始异常检测")
    # 先清除旧的未处理(PENDING)异常，避免重复堆积
    execute_sql("DELETE FROM anomaly_alert WHERE status=0")
    new_alerts = 0
    # 1. 库存异常：销量突增/库存为0
    invs = query_to_dicts("""
        SELECT i.product_id, i.quantity, i.safety_stock, p.product_name,
               (SELECT COALESCE(SUM(quantity),0) FROM inventory_record r
                 WHERE r.product_id=i.product_id AND r.type='OUT'
                   AND r.create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)) AS out_7d
        FROM inventory i JOIN product p ON p.id = i.product_id
        WHERE p.deleted=0
    """)
    if invs:
        X = np.array([[r["quantity"], int(r["safety_stock"]), int(r["out_7d"] or 0)] for r in invs], dtype=float)
        if len(X) >= 3:
            iso = IsolationForest(contamination=0.2, random_state=42)
            preds = iso.fit_predict(X)
            scores = iso.decision_function(X)
            for i, r in enumerate(invs):
                is_anom = preds[i] == -1
                qty = int(r["quantity"])
                if qty == 0:
                    desc = f"商品「{r['product_name']}」库存为0，已触发缺货紧急预警"
                    _insert_alert("INVENTORY", r["product_id"], 0.95, desc)
                    new_alerts += 1
                elif qty < int(r["safety_stock"]):
                    desc = f"商品「{r['product_name']}」库存低于安全库存（当前{qty}/安全{r['safety_stock']}）"
                    _insert_alert("INVENTORY", r["product_id"], 0.8, desc)
                    new_alerts += 1
                elif is_anom:
                    desc = f"商品「{r['product_name']}」近7日出库量{int(r['out_7d'])}，检测到库存/销量异常波动（score={float(scores[i]):.2f}）"
                    _insert_alert("INVENTORY", r["product_id"], round(float(abs(scores[i])), 2), desc)
                    new_alerts += 1

    # 2. 采购单延迟异常
    pos = query_to_dicts("""
        SELECT id, order_no, create_time, status, DATEDIFF(NOW(), create_time) AS days
        FROM purchase_order
        WHERE status IN ('APPROVED','SHIPPED') AND DATEDIFF(NOW(), create_time) > 10
    """)
    for po in pos:
        desc = f"采购单 {po['order_no']} 已{int(po['days'])}天未完成（状态{po['status']}），存在交付延迟风险"
        _insert_alert("PURCHASE", None, 0.7, desc, po["order_no"])
        new_alerts += 1

    # 3. 订单异常金额（离群）
    orders = query_to_dicts("SELECT id, order_no, total_amount FROM orders WHERE create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)")
    if len(orders) >= 5:
        amounts = np.array([[float(o["total_amount"])] for o in orders], dtype=float)
        iso = IsolationForest(contamination=0.05, random_state=42)
        preds = iso.fit_predict(amounts)
        scores = iso.decision_function(amounts)
        for i, o in enumerate(orders):
            if preds[i] == -1 and float(o["total_amount"]) > np.median(amounts) * 3:
                desc = f"订单 {o['order_no']} 金额{float(o['total_amount'])}元异常偏高（高于中位数3倍），疑似异常订单"
                _insert_alert("ORDER", None, round(float(abs(scores[i])), 2), desc, o["order_no"])
                new_alerts += 1

    log.info("异常检测完成，新增预警 %d 条", new_alerts)
    return {"code": 200, "message": f"异常检测完成，新增{new_alerts}条预警", "data": {"newAlerts": new_alerts}}


def _insert_alert(alert_type, product_id, score, description, ref_order_no=None):
    # 避免重复：检查最近24小时内是否已存在同类型同商品/同单号未处理预警
    chk_sql = "SELECT id FROM anomaly_alert WHERE alert_type=:t AND status=0 AND description=:d AND create_time > DATE_SUB(NOW(), INTERVAL 24 HOUR)"
    params = {"t": alert_type, "d": description}
    existing = query_to_dicts(chk_sql, params)
    if existing:
        return
    execute_sql("""
        INSERT INTO anomaly_alert (alert_type, product_id, ref_order_no, anomaly_score, description, status, create_time)
        VALUES (:t, :pid, :ref, :sc, :d, 0, NOW())
    """, {"t": alert_type, "pid": product_id, "ref": ref_order_no, "sc": score, "d": description})


@app.get("/api/v1/ai/anomaly", response_model=Dict[str, Any])
def list_anomalies(status: Optional[str] = Query(None),
                   alertType: Optional[str] = Query(None),
                   type: Optional[str] = Query(None),
                   pageNum: int = 1, pageSize: int = 20):
    where = "WHERE 1=1"
    params = {}
    # 前端传字符串状态 PENDING/HANDLED
    if status is not None and status != "":
        if status == "PENDING":
            where += " AND a.status=0"; params["st"] = 0
        elif status == "HANDLED":
            where += " AND a.status=1"; params["st"] = 1
        else:
            try:
                where += " AND a.status=:st"; params["st"] = int(status)
            except ValueError:
                pass
    atype = alertType if alertType else type
    if atype:
        where += " AND a.alert_type=:tp"; params["tp"] = atype
    total = query_to_dicts(f"SELECT COUNT(*) c FROM anomaly_alert a {where}", params)[0]["c"]
    sql = f"""
    SELECT a.*, p.product_name FROM anomaly_alert a
    LEFT JOIN product p ON p.id=a.product_id
    {where}
    ORDER BY a.create_time DESC
    LIMIT :offset,:limit
    """
    params.update({"offset": (pageNum-1)*pageSize, "limit": pageSize})
    rows = query_to_dicts(sql, params)
    data = []
    for r in rows:
        target = r["product_name"] if r["product_id"] else (r["ref_order_no"] or "-")
        data.append({
            "id": r["id"],
            "type": r["alert_type"],
            "alertType": r["alert_type"],
            "productId": r["product_id"],
            "productName": r["product_name"],
            "refOrderNo": r["ref_order_no"],
            "target": target,
            "score": float(r["anomaly_score"]),
            "anomalyScore": float(r["anomaly_score"]),
            "description": r["description"],
            "status": "PENDING" if int(r["status"]) == 0 else "HANDLED",
            "statusCode": int(r["status"]),
            "createTime": r["create_time"].strftime("%Y-%m-%d %H:%M:%S") if r["create_time"] else None
        })
    return {"code": 200, "message": "success", "data": {"total": total, "list": data}}


@app.put("/api/v1/ai/anomaly/{aid}/handle")
def handle_anomaly(aid: int):
    execute_sql("UPDATE anomaly_alert SET status=1 WHERE id=:id", {"id": aid})
    return {"code": 200, "message": "已处理"}


# ---------- Dashboard 概览（AI 仪表盘） ----------
@app.get("/api/v1/ai/dashboard", response_model=Dict[str, Any])
def ai_dashboard():
    with engine.connect() as conn:
        def v(sql):
            return conn.execute(text(sql)).scalar() or 0
        product_cnt = int(v("SELECT COUNT(*) FROM product WHERE deleted=0 AND status=1"))
        low_stock = int(v("SELECT COUNT(*) FROM inventory WHERE quantity < safety_stock"))
        pending_po = int(v("SELECT COUNT(*) FROM purchase_order WHERE status IN ('PENDING','APPROVED','SHIPPED')"))
        pending_orders = int(v("SELECT COUNT(*) FROM orders WHERE status IN ('PENDING_PAY','PENDING_SHIP')"))
        today_sales = float(v("SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE DATE(create_time)=CURDATE()"))
        unhandled_alerts = int(v("SELECT COUNT(*) FROM anomaly_alert WHERE status=0"))
        today_pred = int(v("SELECT COALESCE(SUM(predict_quantity),0) FROM sales_prediction WHERE predict_date=DATE_ADD(CURDATE(), INTERVAL 1 DAY)"))
    # 近7天销量趋势
    trend = query_to_dicts("""
        SELECT DATE(create_time) d, COALESCE(SUM(total_amount),0) amt, COUNT(*) cnt
        FROM orders WHERE DATE(create_time) >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
        GROUP BY DATE(create_time) ORDER BY d
    """)
    from datetime import timedelta
    trend_map = {str(r["d"]): float(r["amt"]) for r in trend}
    trend_data = []
    for i in range(6, -1, -1):
        d = (date.today() - timedelta(days=i)).strftime("%Y-%m-%d")
        trend_data.append({"date": d, "amount": trend_map.get(d, 0.0), "sales": trend_map.get(d, 0.0)})
    # 热门商品 top5 - 优先从订单明细取，不足则使用库存流水SALE出库数据兜底
    hot = query_to_dicts("""
        SELECT oi.product_id, p.product_name, SUM(oi.quantity) qty
        FROM order_item oi JOIN product p ON p.id=oi.product_id
        JOIN orders o ON o.id=oi.order_id
        WHERE o.create_time >= DATE_SUB(NOW(), INTERVAL 90 DAY) AND o.deleted=0 AND oi.deleted=0
        GROUP BY oi.product_id ORDER BY qty DESC LIMIT 5
    """)
    if not hot or len(hot) < 3:
        hot2 = query_to_dicts("""
            SELECT r.product_id, p.product_name, SUM(r.quantity) qty
            FROM inventory_record r JOIN product p ON p.id=r.product_id
            WHERE r.biz_type='SALE' AND r.type='OUT' AND r.create_time >= DATE_SUB(NOW(), INTERVAL 90 DAY)
            GROUP BY r.product_id ORDER BY qty DESC LIMIT 5
        """)
        existing_pids = {r["product_id"] for r in hot}
        for r in hot2:
            if r["product_id"] not in existing_pids:
                hot.append(r)
                existing_pids.add(r["product_id"])
        hot = sorted(hot, key=lambda x: int(x["qty"]), reverse=True)[:5]
    if not hot:
        hot3 = query_to_dicts("""
            SELECT i.product_id, p.product_name, (i.safety_stock * 2) qty
            FROM inventory i JOIN product p ON p.id=i.product_id
            WHERE p.deleted=0 AND p.status=1
            ORDER BY i.quantity DESC LIMIT 5
        """)
        hot = hot3
    hot_data = []
    for r in hot:
        q = max(int(r["qty"]), 1)
        hot_data.append({"productId": r["product_id"], "productName": r["product_name"], "qty": q, "sales": q})
    return {"code": 200, "message": "success", "data": {
        "stats": {"products": product_cnt, "lowStock": low_stock, "pendingPO": pending_po,
                  "pendingOrders": pending_orders, "todaySales": today_sales,
                  "unhandledAlerts": unhandled_alerts, "tomorrowPredSales": max(today_pred, 1)},
        "trend": trend_data, "hotProducts": hot_data
    }}


@app.get("/healthz")
def health():
    try:
        with engine.connect() as c:
            c.execute(text("SELECT 1"))
        return {"status": "ok", "db": "ok"}
    except Exception as e:
        return {"status": "error", "db": str(e)}


@app.on_event("startup")
def on_start():
    log.info("AI 服务启动，延迟 30 秒后自动执行首次预测/异常检测")
    import threading, time
    def delayed():
        time.sleep(30)
        for _ in range(3):
            try:
                run_prediction(7)
                detect_anomaly()
                log.info("首次 AI 任务执行完成")
                break
            except Exception as e:
                log.warning("首次任务执行失败，稍后重试: %s", e)
                time.sleep(10)
    threading.Thread(target=delayed, daemon=True).start()
