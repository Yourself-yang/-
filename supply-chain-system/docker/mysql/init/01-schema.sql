-- ========================================
-- 智能电商微服务供应链管理系统 - 数据库初始化
-- 数据库: supply_chain  (MySQL 8.0)
-- ========================================
CREATE DATABASE IF NOT EXISTS supply_chain DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE supply_chain;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------- 用户角色权限 ----------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `perm_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
  `perm_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码 ADMIN/MERCHANT/SUPPLIER',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(200) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission` (
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt',
  `real_name` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `email` VARCHAR(100) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1正常/0禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- ---------- 商品分类 ----------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_name` VARCHAR(100) NOT NULL,
  `parent_id` BIGINT DEFAULT 0,
  `sort` INT DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类';

-- ---------- 商品 ----------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_code` VARCHAR(50) NOT NULL,
  `product_name` VARCHAR(200) NOT NULL,
  `category_id` BIGINT NOT NULL,
  `brand` VARCHAR(100) DEFAULT NULL,
  `spec` VARCHAR(100) DEFAULT NULL,
  `unit` VARCHAR(20) NOT NULL DEFAULT '件',
  `purchase_price` DECIMAL(10,2) NOT NULL DEFAULT 0,
  `sale_price` DECIMAL(10,2) NOT NULL DEFAULT 0,
  `image_url` VARCHAR(500) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1上架/0下架',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`),
  KEY `idx_name` (`product_name`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ---------- 供应商 ----------
DROP TABLE IF EXISTS `supplier`;
CREATE TABLE `supplier` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `supplier_name` VARCHAR(200) NOT NULL,
  `contact_person` VARCHAR(50) NOT NULL,
  `contact_phone` VARCHAR(20) NOT NULL,
  `address` VARCHAR(500) DEFAULT NULL,
  `level` TINYINT NOT NULL DEFAULT 3 COMMENT '评级1-5',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1合作中/0停用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- ---------- 采购订单 ----------
DROP TABLE IF EXISTS `purchase_order`;
CREATE TABLE `purchase_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(50) NOT NULL,
  `supplier_id` BIGINT NOT NULL,
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/SHIPPED/RECEIVED/REJECTED',
  `remark` VARCHAR(500) DEFAULT NULL,
  `creator_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `receive_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_supplier` (`supplier_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

DROP TABLE IF EXISTS `purchase_order_item`;
CREATE TABLE `purchase_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `product_name` VARCHAR(200) NOT NULL,
  `quantity` INT NOT NULL,
  `received_quantity` INT NOT NULL DEFAULT 0,
  `unit_price` DECIMAL(10,2) NOT NULL,
  `subtotal` DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单明细';

-- ---------- 库存 ----------
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `quantity` INT NOT NULL DEFAULT 0,
  `safety_stock` INT NOT NULL DEFAULT 10,
  `warehouse` VARCHAR(50) NOT NULL DEFAULT '主仓库',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

DROP TABLE IF EXISTS `inventory_record`;
CREATE TABLE `inventory_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `type` VARCHAR(20) NOT NULL COMMENT 'IN/OUT',
  `biz_type` VARCHAR(30) NOT NULL COMMENT 'PURCHASE/SALE/TRANSFER/CHECK/RETURN',
  `quantity` INT NOT NULL,
  `before_quantity` INT NOT NULL,
  `after_quantity` INT NOT NULL,
  `ref_order_no` VARCHAR(50) DEFAULT NULL,
  `remark` VARCHAR(200) DEFAULT NULL,
  `operator_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_product_time` (`product_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- ---------- 电商订单 ----------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(50) NOT NULL,
  `total_amount` DECIMAL(12,2) NOT NULL DEFAULT 0,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAY' COMMENT 'PENDING_PAY/PENDING_SHIP/SHIPPED/COMPLETED/CANCELLED/REFUNDING',
  `receiver_name` VARCHAR(50) NOT NULL,
  `receiver_phone` VARCHAR(20) NOT NULL,
  `receiver_address` VARCHAR(500) NOT NULL,
  `logistics_no` VARCHAR(50) DEFAULT NULL,
  `pay_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ship_time` DATETIME DEFAULT NULL,
  `finish_time` DATETIME DEFAULT NULL,
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电商订单表';

DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `product_id` BIGINT NOT NULL,
  `product_name` VARCHAR(200) NOT NULL,
  `product_image` VARCHAR(500) DEFAULT NULL,
  `quantity` INT NOT NULL,
  `unit_price` DECIMAL(10,2) NOT NULL,
  `subtotal` DECIMAL(12,2) NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

-- ---------- AI 分析 ----------
DROP TABLE IF EXISTS `sales_prediction`;
CREATE TABLE `sales_prediction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `predict_date` DATE NOT NULL,
  `predict_quantity` INT NOT NULL,
  `model_type` VARCHAR(20) NOT NULL DEFAULT 'MOVING_AVG',
  `confidence` DECIMAL(5,2) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_product_date` (`product_id`,`predict_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销量预测表';

DROP TABLE IF EXISTS `replenishment_suggestion`;
CREATE TABLE `replenishment_suggestion` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `current_stock` INT NOT NULL,
  `predicted_sales` INT NOT NULL,
  `safety_stock` INT NOT NULL,
  `suggested_qty` INT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理/1已生成采购单',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能补货建议';

DROP TABLE IF EXISTS `anomaly_alert`;
CREATE TABLE `anomaly_alert` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `alert_type` VARCHAR(30) NOT NULL COMMENT 'INVENTORY/ORDER/PURCHASE',
  `product_id` BIGINT DEFAULT NULL,
  `ref_order_no` VARCHAR(50) DEFAULT NULL,
  `anomaly_score` DECIMAL(5,2) NOT NULL DEFAULT 0,
  `description` VARCHAR(500) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未处理/1已处理',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type_status` (`alert_type`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常预警表';

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================
-- 初始化数据
-- ========================================
-- 密码：admin / merchant / supplier 均为 123456
-- BCrypt hash generated at build time: 以下为 $2b$10$ 加密后的 123456
-- 实际值在应用启动时由 DataInitializer 写入，这里先用固定 hash
-- 使用 hash: $2a$10$04OuMjM3M3h.TfXAQjfCTeoUOD.i0uryCYXFbgJDZWUHO5rG.t2PK  (BCrypt of "123456")
INSERT INTO `role` (`id`,`role_code`,`role_name`,`description`) VALUES
(1,'ADMIN','系统管理员','拥有全部权限'),
(2,'MERCHANT','商家/运营','商品、采购、库存、订单、AI查看'),
(3,'SUPPLIER','供应商','查看采购订单、确认供货');

INSERT INTO `permission` (`perm_code`,`perm_name`) VALUES
('user:view','用户查看'),('user:edit','用户编辑'),
('product:view','商品查看'),('product:edit','商品编辑'),
('purchase:view','采购查看'),('purchase:edit','采购编辑'),('purchase:approve','采购审核'),
('inventory:view','库存查看'),('inventory:edit','库存操作'),
('order:view','订单查看'),('order:ship','订单发货'),('order:refund','退款处理'),
('ai:view','AI分析查看'),('ai:run','AI任务执行');

INSERT INTO `role_permission` VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),(1,10),(1,11),(1,12),(1,13),(1,14),
(2,3),(2,4),(2,5),(2,6),(2,8),(2,9),(2,10),(2,11),(2,13),
(3,5),(3,8),(3,10),(3,13);

INSERT INTO `user` (`username`,`password`,`real_name`,`phone`,`email`,`status`) VALUES
('admin','$2a$10$04OuMjM3M3h.TfXAQjfCTeoUOD.i0uryCYXFbgJDZWUHO5rG.t2PK','系统管理员','13800000001','admin@supplychain.com',1),
('merchant','$2a$10$04OuMjM3M3h.TfXAQjfCTeoUOD.i0uryCYXFbgJDZWUHO5rG.t2PK','张运营','13800000002','merchant@supplychain.com',1),
('supplier','$2a$10$04OuMjM3M3h.TfXAQjfCTeoUOD.i0uryCYXFbgJDZWUHO5rG.t2PK','李供应商','13800000003','supplier@supplychain.com',1);

INSERT INTO `user_role` VALUES (1,1),(2,2),(3,3);

INSERT INTO `category` (`id`,`category_name`,`parent_id`,`sort`) VALUES
(1,'食品饮料',0,1),(2,'日用百货',0,2),(3,'电子产品',0,3),(4,'服装鞋帽',0,4),
(5,'休闲零食',1,1),(6,'饮料冲调',1,2),(7,'清洁用品',2,1),(8,'纸品湿巾',2,2),
(9,'手机数码',3,1),(10,'电脑办公',3,2);

INSERT INTO `supplier` (`supplier_name`,`contact_person`,`contact_phone`,`address`,`level`) VALUES
('优选食品有限公司','王经理','13900000001','上海市浦东新区张江路100号',5),
('恒达日用品厂','赵经理','13900000002','浙江省义乌市工业区A栋',4),
('华科电子科技','陈经理','13900000003','深圳市南山区科技园B座',5),
('潮流服饰供应链','刘经理','13900000004','广州市白云区服装城3号',3);

INSERT INTO `product` (`product_code`,`product_name`,`category_id`,`brand`,`spec`,`unit`,`purchase_price`,`sale_price`,`image_url`,`status`) VALUES
('SP001','三只松鼠每日坚果750g',5,'三只松鼠','750g/箱','箱',68.00,99.00,'https://img14.360buyimg.com/n1/jfs/t1/196728/25/25333/301711/648d8c94F8290d5c2/9c7f20c76d0d5f5a.jpg',1),
('SP002','农夫山泉饮用水550ml*24',6,'农夫山泉','550ml*24瓶','箱',28.00,42.00,'https://img10.360buyimg.com/n1/jfs/t1/186889/10/27046/148746/64ad3b02F9b2d7c8c/5b5d3c2b1a0f9e8d.jpg',1),
('SP003','伊利纯牛奶250ml*16',6,'伊利','250ml*16盒','箱',45.00,69.90,'https://img14.360buyimg.com/n1/jfs/t1/177990/36/26477/264376/64a6f8c3Fb7e6d5a4/4c3b2a1f0e9d8c7b.jpg',1),
('SP004','蓝月亮洗衣液3kg',7,'蓝月亮','3kg/瓶','瓶',32.00,49.90,'https://img11.360buyimg.com/n1/jfs/t1/206610/36/25263/223444/64d3f4c7F1a2b3c4d/9e8d7c6b5a4f3e2d.jpg',1),
('SP005','维达抽纸3层130抽*24包',8,'维达','130抽*24包','箱',48.00,72.00,'https://img12.360buyimg.com/n1/jfs/t1/196728/25/25334/267312/648d8d95Fc3b2a1d0/8d7c6b5a4f3e2d1c.jpg',1),
('SP006','Apple iPhone 15 128G',9,'Apple','128G 黑色','台',5299.00,5999.00,'https://img14.360buyimg.com/n1/jfs/t1/196728/25/25335/301712/648d8e96Fa4c3b2d1/7c6b5a4f3e2d1c0b.jpg',1),
('SP007','华为MateBook 14笔记本',10,'华为','i5/16G/512G','台',4800.00,5699.00,'https://img13.360buyimg.com/n1/jfs/t1/186889/10/27047/148747/64ad3c03Fa5b4c3d2/6b5a4f3e2d1c0b9a.jpg',1),
('SP008','良品铺子猪肉脯100g',5,'良品铺子','100g/袋','袋',12.00,19.90,'https://img12.360buyimg.com/n1/jfs/t1/177990/36/26478/264377/64a6f9d4Fc6d5e4b3/5a4f3b2c1d0e9f8a.jpg',1),
('SP009','海飞丝洗发水750ml',7,'海飞丝','750ml/瓶','瓶',42.00,65.00,'https://img10.360buyimg.com/n1/jfs/t1/206610/36/25264/223445/64d3f5d8F2b3c4d5e/4f3e2d1c0b9a8f7e.jpg',0),
('SP010','优衣库纯棉T恤男款',4,'优衣库','M/L/XL','件',45.00,99.00,'https://img14.360buyimg.com/n1/jfs/t1/196728/25/25336/301713/648d8f97Gb5d4e3f2/3e2d1c0b9a8f7e6d.jpg',1);

INSERT INTO `inventory` (`product_id`,`quantity`,`safety_stock`,`warehouse`) VALUES
(1,120,30,'主仓库'),(2,200,50,'主仓库'),(3,80,40,'主仓库'),
(4,5,20,'主仓库'),(5,60,25,'主仓库'),(6,15,10,'主仓库'),
(7,8,5,'主仓库'),(8,0,50,'主仓库'),(9,35,15,'主仓库'),(10,90,30,'主仓库');

-- 电商演示订单
INSERT INTO `orders` (`order_no`,`total_amount`,`status`,`receiver_name`,`receiver_phone`,`receiver_address`,`pay_time`,`create_time`) VALUES
('EC20260801001',99.00,'COMPLETED','王小明','13700000001','北京市朝阳区建国路1号','2026-08-01 10:15:00','2026-08-01 10:10:00'),
('EC20260802001',42.00,'SHIPPED','李小红','13700000002','上海市徐汇区衡山路88号','2026-08-02 14:20:00','2026-08-02 14:15:00'),
('EC20260803001',138.90,'PENDING_SHIP','周小华','13700000003','广州市天河区体育西路33号','2026-08-03 09:05:00','2026-08-03 09:00:00'),
('EC20260815001',5999.00,'SHIPPED','陈大伟','13700000004','深圳市南山区科技园路99号','2026-08-15 11:30:00','2026-08-15 11:25:00'),
('EC20260820001',72.00,'PENDING_PAY','赵小刚','13700000005','杭州市西湖区文三路168号',NULL,'2026-08-31 16:40:00'),
('EC20260825001',198.00,'COMPLETED','孙小美','13700000006','成都市锦江区春熙路58号','2026-08-25 13:10:00','2026-08-25 13:05:00');

INSERT INTO `order_item` (`order_id`,`product_id`,`product_name`,`quantity`,`unit_price`,`subtotal`) VALUES
(1,1,'三只松鼠每日坚果750g',1,99.00,99.00),
(2,2,'农夫山泉饮用水550ml*24',1,42.00,42.00),
(3,1,'三只松鼠每日坚果750g',1,99.00,99.00),
(3,3,'伊利纯牛奶250ml*16',1,39.90,39.90),
(4,6,'Apple iPhone 15 128G',1,5999.00,5999.00),
(5,5,'维达抽纸3层130抽*24包',1,72.00,72.00),
(6,1,'三只松鼠每日坚果750g',2,99.00,198.00);

-- 采购演示订单
INSERT INTO `purchase_order` (`order_no`,`supplier_id`,`total_amount`,`status`,`creator_id`,`create_time`,`receive_time`) VALUES
('PO20260801001',1,6800.00,'RECEIVED',2,'2026-08-01 09:00:00','2026-08-05 14:00:00'),
('PO20260810001',2,1600.00,'APPROVED',2,'2026-08-10 11:00:00',NULL),
('PO20260820001',3,48000.00,'SHIPPED',2,'2026-08-20 15:30:00',NULL),
('PO20260828001',1,3400.00,'PENDING',2,'2026-08-28 10:20:00',NULL);

INSERT INTO `purchase_order_item` (`order_id`,`product_id`,`product_name`,`quantity`,`received_quantity`,`unit_price`,`subtotal`) VALUES
(1,1,'三只松鼠每日坚果750g',100,100,68.00,6800.00),
(2,4,'蓝月亮洗衣液3kg',50,0,32.00,1600.00),
(3,7,'华为MateBook 14笔记本',10,0,4800.00,48000.00),
(4,8,'良品铺子猪肉脯100g',200,0,12.00,2400.00),
(4,3,'伊利纯牛奶250ml*16',22,0,45.00,990.00);

-- 库存流水演示
INSERT INTO `inventory_record` (`product_id`,`type`,`biz_type`,`quantity`,`before_quantity`,`after_quantity`,`ref_order_no`,`operator_id`,`create_time`) VALUES
(1,'IN','PURCHASE',100,20,120,'PO20260801001',2,'2026-08-05 14:00:00'),
(1,'OUT','SALE',1,121,120,'EC20260801001',2,'2026-08-01 10:15:00'),
(2,'OUT','SALE',1,201,200,'EC20260802001',2,'2026-08-02 14:20:00'),
(6,'OUT','SALE',1,16,15,'EC20260815001',2,'2026-08-15 11:30:00'),
(4,'OUT','SALE',5,10,5,NULL,2,'2026-08-20 10:00:00'),
(8,'OUT','SALE',20,20,0,NULL,2,'2026-08-22 09:00:00');

-- 演示异常预警
INSERT INTO `anomaly_alert` (`alert_type`,`product_id`,`anomaly_score`,`description`,`create_time`) VALUES
('INVENTORY',4,0.85,'商品「蓝月亮洗衣液3kg」库存低于安全库存（当前5/安全20），请及时补货','2026-08-30 08:00:00'),
('INVENTORY',8,0.92,'商品「良品铺子猪肉脯100g」库存为0，已触发缺货紧急预警','2026-08-30 08:00:00'),
('PURCHASE',NULL,0.72,'采购单 PO20260810001 已审核超过15天未到货，存在交付延迟风险','2026-08-28 08:00:00');
