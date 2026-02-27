-- ============================================
-- NL2SQL 示例数据 - 插入测试数据
-- ============================================

-- 插入分类数据
INSERT INTO categories (category_id, category_name, parent_id, level, sort_order, description) VALUES
(1, '电子产品', 0, 1, 1, '手机、电脑、数码配件等电子产品'),
(2, '手机', 1, 2, 1, '智能手机、功能手机'),
(3, '电脑', 1, 2, 2, '笔记本电脑、台式机、平板电脑'),
(4, '数码配件', 1, 2, 3, '耳机、充电器、数据线等'),
(5, '服装', 0, 1, 2, '男装、女装、童装等'),
(6, '男装', 5, 2, 1, '男士上衣、裤子、外套'),
(7, '女装', 5, 2, 2, '女士上衣、裙子、外套'),
(8, '食品', 0, 1, 3, '零食、饮料、生鲜'),
(9, '零食', 8, 2, 1, '饼干、糖果、坚果'),
(10, '饮料', 8, 2, 2, '碳酸饮料、果汁、茶饮');

-- 插入商品数据
INSERT INTO products (product_id, product_name, category_id, brand, description, price, original_price, stock_quantity, sold_quantity, status, main_image_url, weight, sku, is_hot, is_new, rating, review_count) VALUES
(1, 'iPhone 15 Pro Max', 2, 'Apple', '苹果最新旗舰手机，A17 Pro芯片，钛金属设计', 9999.00, 10999.00, 500, 1200, 1, 'https://example.com/iphone15.jpg', 0.22, 'APPLE-001', 1, 1, 4.8, 856),
(2, 'iPhone 15', 2, 'Apple', '苹果标准版手机，A16芯片，灵动岛设计', 5999.00, 6999.00, 800, 2100, 1, 'https://example.com/iphone15.jpg', 0.17, 'APPLE-002', 1, 1, 4.7, 1203),
(3, 'MacBook Pro 16英寸', 3, 'Apple', '专业级笔记本电脑，M3 Max芯片', 24999.00, 26999.00, 100, 350, 1, 'https://example.com/macbook.jpg', 2.14, 'APPLE-003', 1, 1, 4.9, 234),
(4, 'AirPods Pro 2', 4, 'Apple', '主动降噪无线耳机，H2芯片', 1899.00, 2199.00, 1200, 5600, 1, 'https://example.com/airpods.jpg', 0.05, 'APPLE-004', 1, 0, 4.6, 3421),
(5, '华为Mate 60 Pro', 2, '华为', '华为旗舰手机，卫星通话，玄武架构', 6999.00, 7999.00, 300, 890, 1, 'https://example.com/mate60.jpg', 0.20, 'HUAWEI-001', 1, 1, 4.7, 678),
(6, '小米14 Pro', 2, '小米', '徕卡影像，骁龙8 Gen3', 4999.00, 5499.00, 600, 1500, 1, 'https://example.com/mi14.jpg', 0.19, 'XIAOMI-001', 1, 1, 4.5, 892),
(7, 'ThinkPad X1 Carbon', 3, '联想', '商务旗舰笔记本，轻薄便携', 12999.00, 14999.00, 200, 450, 1, 'https://example.com/thinkpad.jpg', 1.12, 'LENOVO-001', 0, 0, 4.4, 567),
(8, 'Sony WH-1000XM5', 4, '索尼', '旗舰降噪耳机，30小时续航', 2499.00, 2999.00, 400, 1200, 1, 'https://example.com/sony.jpg', 0.25, 'SONY-001', 1, 0, 4.6, 1234),
(9, '纯棉T恤男款', 6, '优衣库', '舒适透气，基础百搭', 99.00, 129.00, 2000, 8500, 1, 'https://example.com/tshirt.jpg', 0.20, 'UNIQLO-001', 0, 0, 4.3, 2341),
(10, '连衣裙夏季新款', 7, 'ZARA', '时尚设计，清凉舒适', 299.00, 399.00, 500, 2100, 1, 'https://example.com/dress.jpg', 0.30, 'ZARA-001', 0, 1, 4.2, 876),
(11, '奥利奥饼干', 9, '亿滋', '经典夹心饼干', 15.90, 19.90, 5000, 15000, 1, 'https://example.com/oreo.jpg', 0.15, 'YIZI-001', 1, 0, 4.7, 5678),
(12, '可口可乐330ml', 10, '可口可乐', '经典碳酸饮料', 3.50, 4.00, 10000, 50000, 1, 'https://example.com/coke.jpg', 0.33, 'COCA-001', 1, 0, 4.5, 8901);

-- 插入用户数据
INSERT INTO users (user_id, username, email, phone, password_hash, status, created_at, avatar_url, gender, birth_date, country, city) VALUES
(1, 'zhangsan', 'zhangsan@example.com', '13800138001', 'hash123', 1, '2023-01-15 10:30:00', 'https://example.com/avatar1.jpg', 1, '1990-05-20', '中国', '北京'),
(2, 'lisi', 'lisi@example.com', '13800138002', 'hash456', 1, '2023-02-20 14:20:00', 'https://example.com/avatar2.jpg', 2, '1992-08-15', '中国', '上海'),
(3, 'wangwu', 'wangwu@example.com', '13800138003', 'hash789', 1, '2023-03-10 09:15:00', 'https://example.com/avatar3.jpg', 1, '1988-12-01', '中国', '广州'),
(4, 'zhaoliu', 'zhaoliu@example.com', '13800138004', 'hashabc', 1, '2023-06-05 16:45:00', 'https://example.com/avatar4.jpg', 2, '1995-03-25', '中国', '深圳'),
(5, 'qianqi', 'qianqi@example.com', '13800138005', 'hashdef', 1, '2024-01-10 11:00:00', 'https://example.com/avatar5.jpg', 1, '1993-07-18', '中国', '杭州'),
(6, 'sunba', 'sunba@example.com', '13800138006', 'hashghi', 0, '2023-08-12 13:30:00', 'https://example.com/avatar6.jpg', 2, '1991-11-30', '中国', '成都'),
(7, 'zhoujiu', 'zhoujiu@example.com', '13800138007', 'hashjkl', 1, '2024-02-15 10:00:00', 'https://example.com/avatar7.jpg', 1, '1994-09-10', '中国', '南京'),
(8, 'wushi', 'wushi@example.com', '13800138008', 'hashmno', 1, '2023-11-20 15:20:00', 'https://example.com/avatar8.jpg', 2, '1989-04-05', '中国', '武汉');

-- 插入订单数据
INSERT INTO orders (order_id, order_no, user_id, order_status, total_amount, discount_amount, shipping_fee, pay_amount, pay_time, created_at, shipping_address, receiver_name, receiver_phone, remark, pay_method) VALUES
(1, '202401010001', 1, 3, 10198.00, 200.00, 0.00, 9998.00, '2024-01-01 10:35:00', '2024-01-01 10:30:00', '北京市朝阳区xxx街道xxx号', '张三', '13800138001', '', 1),
(2, '202401020001', 2, 3, 1899.00, 0.00, 0.00, 1899.00, '2024-01-02 14:25:00', '2024-01-02 14:20:00', '上海市浦东新区xxx路xxx号', '李四', '13800138002', '请尽快发货', 2),
(3, '202401050001', 1, 3, 4999.00, 0.00, 0.00, 4999.00, '2024-01-05 09:20:00', '2024-01-05 09:15:00', '北京市朝阳区xxx街道xxx号', '张三', '13800138001', '', 1),
(4, '202402100001', 3, 3, 6999.00, 0.00, 0.00, 6999.00, '2024-02-10 16:50:00', '2024-02-10 16:45:00', '广州市天河区xxx路xxx号', '王五', '13800138003', '', 1),
(5, '202402150001', 4, 2, 24999.00, 1000.00, 0.00, 23999.00, '2024-02-15 11:05:00', '2024-02-15 11:00:00', '深圳市南山区xxx路xxx号', '赵六', '13800138004', '公司采购', 3),
(6, '202403010001', 2, 3, 299.00, 0.00, 10.00, 309.00, '2024-03-01 20:15:00', '2024-03-01 20:10:00', '上海市浦东新区xxx路xxx号', '李四', '13800138002', '', 2),
(7, '202403200001', 5, 1, 12999.00, 0.00, 0.00, 12999.00, '2024-03-20 10:30:00', '2024-03-20 10:25:00', '杭州市西湖区xxx路xxx号', '钱七', '13800138005', '', 1),
(8, '202404050001', 7, 0, 5999.00, 0.00, 0.00, 5999.00, NULL, '2024-04-05 15:00:00', '南京市鼓楼区xxx路xxx号', '周九', '13800138007', '', NULL);

-- 插入订单商品数据
INSERT INTO order_items (item_id, order_id, product_id, product_name, product_image, price, quantity, subtotal) VALUES
(1, 1, 1, 'iPhone 15 Pro Max', 'https://example.com/iphone15.jpg', 9999.00, 1, 9999.00),
(2, 1, 4, 'AirPods Pro 2', 'https://example.com/airpods.jpg', 1899.00, 1, 1899.00),
(3, 2, 4, 'AirPods Pro 2', 'https://example.com/airpods.jpg', 1899.00, 1, 1899.00),
(4, 3, 6, '小米14 Pro', 'https://example.com/mi14.jpg', 4999.00, 1, 4999.00),
(5, 4, 5, '华为Mate 60 Pro', 'https://example.com/mate60.jpg', 6999.00, 1, 6999.00),
(6, 5, 3, 'MacBook Pro 16英寸', 'https://example.com/macbook.jpg', 24999.00, 1, 24999.00),
(7, 6, 10, '连衣裙夏季新款', 'https://example.com/dress.jpg', 299.00, 1, 299.00),
(8, 7, 7, 'ThinkPad X1 Carbon', 'https://example.com/thinkpad.jpg', 12999.00, 1, 12999.00),
(9, 8, 2, 'iPhone 15', 'https://example.com/iphone15.jpg', 5999.00, 1, 5999.00);
