package com.supplychain.order.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supplychain.common.exception.BusinessException;
import com.supplychain.order.dto.OrderCreateDTO;
import com.supplychain.order.dto.OrderItemDTO;
import com.supplychain.order.dto.OrderQueryDTO;
import com.supplychain.order.dto.ShipDTO;
import com.supplychain.order.entity.Order;
import com.supplychain.order.entity.OrderItem;
import com.supplychain.order.feign.AiFeignClient;
import com.supplychain.order.feign.InventoryFeignClient;
import com.supplychain.order.mapper.OrderItemMapper;
import com.supplychain.order.mapper.OrderMapper;
import com.supplychain.order.service.OrderService;
import com.supplychain.order.vo.OrderItemVO;
import com.supplychain.order.vo.OrderVO;
import com.supplychain.order.vo.StatVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InventoryFeignClient inventoryFeignClient;

    @Autowired
    private AiFeignClient aiFeignClient;

    private static final String STATUS_PENDING_PAY = "PENDING_PAY";
    private static final String STATUS_PENDING_SHIP = "PENDING_SHIP";
    private static final String STATUS_SHIPPED = "SHIPPED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_REFUNDING = "REFUNDING";

    @Override
    public IPage<OrderVO> page(OrderQueryDTO queryDTO) {
        Page<Order> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Order::getOrderNo, queryDTO.getKeyword())
                    .or().like(Order::getReceiverName, queryDTO.getKeyword())
                    .or().like(Order::getReceiverPhone, queryDTO.getKeyword()));
        }

        if (StrUtil.isNotBlank(queryDTO.getStatus())) {
            wrapper.eq(Order::getStatus, queryDTO.getStatus());
        }

        if (StrUtil.isNotBlank(queryDTO.getStartTime())) {
            try {
                wrapper.ge(Order::getCreateTime, LocalDateTime.parse(queryDTO.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } catch (Exception e) {
                try {
                    wrapper.ge(Order::getCreateTime, LocalDateTime.parse(queryDTO.getStartTime(), DateTimeFormatter.ISO_DATE_TIME));
                } catch (Exception ignored) {}
            }
        }

        if (StrUtil.isNotBlank(queryDTO.getEndTime())) {
            try {
                wrapper.le(Order::getCreateTime, LocalDateTime.parse(queryDTO.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } catch (Exception e) {
                try {
                    wrapper.le(Order::getCreateTime, LocalDateTime.parse(queryDTO.getEndTime(), DateTimeFormatter.ISO_DATE_TIME));
                } catch (Exception ignored) {}
            }
        }

        wrapper.orderByDesc(Order::getCreateTime);
        IPage<Order> orderPage = orderMapper.selectPage(page, wrapper);

        List<OrderVO> records = orderPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        if (!records.isEmpty()) {
            List<Long> orderIds = records.stream().map(OrderVO::getId).collect(Collectors.toList());
            List<OrderItem> items = orderItemMapper
                    .selectList(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
            Map<Long, List<OrderItemVO>> itemMap = items.stream().map(this::convertItemToVO)
                    .collect(Collectors.groupingBy(OrderItemVO::getOrderId));
            records.forEach(vo -> vo.setItems(itemMap.getOrDefault(vo.getId(), Collections.emptyList())));
        }

        Page<OrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order create(OrderCreateDTO createDTO, Long operatorId) {
        String orderNo = "EC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + RandomUtil.randomNumbers(4);

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setStatus(STATUS_PENDING_PAY);
        order.setReceiverName(createDTO.getReceiverName());
        order.setReceiverPhone(createDTO.getReceiverPhone());
        order.setReceiverAddress(createDTO.getReceiverAddress());
        order.setTotalAmount(BigDecimal.ZERO);
        orderMapper.insert(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO itemDTO : createDTO.getItems()) {
            String sql = "SELECT product_name, sale_price, image_url FROM product WHERE id = ? AND deleted = 0";
            Map<String, Object> productMap = jdbcTemplate.queryForMap(sql, itemDTO.getProductId());
            if (productMap == null) {
                throw new BusinessException("商品不存在: " + itemDTO.getProductId());
            }

            String productName = (String) productMap.get("product_name");
            BigDecimal salePrice = (BigDecimal) productMap.get("sale_price");
            String productImage = (String) productMap.get("image_url");
            BigDecimal subtotal = salePrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(itemDTO.getProductId());
            item.setProductName(productName);
            item.setProductImage(productImage);
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(salePrice);
            item.setSubtotal(subtotal);
            orderItemMapper.insert(item);

            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);
        orderMapper.updateById(order);

        try {
            aiFeignClient.triggerPrediction();
        } catch (Exception e) {
            log.warn("触发AI预测失败，不影响订单创建: {}", e.getMessage());
        }

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!STATUS_PENDING_PAY.equals(order.getStatus())) {
            throw new BusinessException("订单状态不支持付款");
        }

        order.setStatus(STATUS_PENDING_SHIP);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long id, ShipDTO shipDTO) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!STATUS_PENDING_SHIP.equals(order.getStatus())) {
            throw new BusinessException("订单状态不支持发货");
        }

        List<OrderItem> items = orderItemMapper
                .selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        for (OrderItem item : items) {
            Map<String, Object> body = new HashMap<>();
            body.put("productId", item.getProductId());
            body.put("type", "OUT");
            body.put("bizType", "SALE");
            body.put("quantity", item.getQuantity());
            body.put("refOrderNo", order.getOrderNo());

            Object result = inventoryFeignClient.changeStock(body);
            if (result instanceof Map) {
                Map<?, ?> resultMap = (Map<?, ?>) result;
                Object code = resultMap.get("code");
                if (code == null || !Integer.valueOf(200).equals(code)) {
                    throw new BusinessException("扣减库存失败: " + item.getProductName());
                }
            } else {
                throw new BusinessException("扣减库存调用失败: " + item.getProductName());
            }
        }

        order.setStatus(STATUS_SHIPPED);
        order.setLogisticsNo(shipDTO.getLogisticsNo());
        order.setShipTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!STATUS_SHIPPED.equals(order.getStatus())) {
            throw new BusinessException("订单状态不支持确认收货");
        }

        order.setStatus(STATUS_COMPLETED);
        order.setFinishTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!STATUS_PENDING_PAY.equals(order.getStatus())) {
            throw new BusinessException("只有待付款订单可以取消");
        }

        order.setStatus(STATUS_CANCELLED);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (STATUS_PENDING_SHIP.equals(order.getStatus())) {
            order.setStatus(STATUS_CANCELLED);
            orderMapper.updateById(order);
        } else if (STATUS_SHIPPED.equals(order.getStatus())) {
            List<OrderItem> items = orderItemMapper
                    .selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
            for (OrderItem item : items) {
                try {
                    Map<String, Object> body = new HashMap<>();
                    body.put("productId", item.getProductId());
                    body.put("type", "IN");
                    body.put("bizType", "RETURN");
                    body.put("quantity", item.getQuantity());
                    body.put("refOrderNo", order.getOrderNo());
                    inventoryFeignClient.changeStock(body);
                } catch (Exception e) {
                    log.error("恢复库存失败: productId={}, error={}", item.getProductId(), e.getMessage());
                    throw new BusinessException("恢复库存失败，退款失败");
                }
            }
            order.setStatus(STATUS_CANCELLED);
            orderMapper.updateById(order);
        } else {
            throw new BusinessException("订单状态不支持退款");
        }
    }

    @Override
    public OrderVO getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        OrderVO vo = convertToVO(order);
        List<OrderItem> items = orderItemMapper
                .selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        vo.setItems(items.stream().map(this::convertItemToVO).collect(Collectors.toList()));
        return vo;
    }

    @Override
    public StatVO stat() {
        StatVO stat = new StatVO();

        stat.setTotalOrders(orderMapper.selectCount(new LambdaQueryWrapper<>()));
        stat.setPendingShip(
                orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, STATUS_PENDING_SHIP)));
        stat.setShipped(orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, STATUS_SHIPPED)));
        stat.setCompleted(
                orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, STATUS_COMPLETED)));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LambdaQueryWrapper<Order> todayWrapper = new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
                .lt(Order::getCreateTime, tomorrowStart);
        stat.setTodayOrders(orderMapper.selectCount(todayWrapper));

        List<Order> todayCompletedOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, STATUS_COMPLETED)
                .ge(Order::getFinishTime, todayStart)
                .lt(Order::getFinishTime, tomorrowStart));
        BigDecimal todaySales = todayCompletedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stat.setTodaySales(todaySales);

        return stat;
    }

    @Override
    public List<Map<String, Object>> getRecentSales(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        String sql = "SELECT DATE(create_time) as date, COALESCE(SUM(total_amount), 0) as dailySales " +
                "FROM orders WHERE status = 'COMPLETED' AND deleted = 0 " +
                "AND DATE(create_time) >= ? GROUP BY DATE(create_time) ORDER BY date";
        return jdbcTemplate.queryForList(sql, startDate);
    }

    @Override
    public List<Map<String, Object>> listAllCompleted(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT oi.product_id, oi.product_name, DATE(o.create_time) as date, SUM(oi.quantity) as quantity "
                +
                "FROM orders o JOIN order_item oi ON o.id = oi.order_id " +
                "WHERE o.status = 'COMPLETED' AND o.deleted = 0 AND oi.deleted = 0 " +
                "AND DATE(o.create_time) >= ? AND DATE(o.create_time) <= ? " +
                "GROUP BY oi.product_id, DATE(o.create_time) ORDER BY date";
        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    private OrderItemVO convertItemToVO(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }
}
