-- ============================================
-- NL2SQL 示例数据
-- 电商系统数据库表结构
-- 包含: users, orders, order_items, products, categories
-- ============================================

-- 1. 用户表 (users)
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_at DATETIME COMMENT '最后登录时间',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    gender TINYINT COMMENT '性别: 0-未知, 1-男, 2-女',
    birth_date DATE COMMENT '生日',
    country VARCHAR(50) COMMENT '国家',
    city VARCHAR(50) COMMENT '城市',
    address VARCHAR(255) COMMENT '详细地址'
) COMMENT='用户表，存储注册用户的基本信息';

-- 2. 商品分类表 (categories)
CREATE TABLE categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id INT DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    level TINYINT DEFAULT 1 COMMENT '分类层级',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    icon_url VARCHAR(255) COMMENT '分类图标URL',
    description VARCHAR(500) COMMENT '分类描述'
) COMMENT='商品分类表，存储商品分类信息，支持多级分类';

-- 3. 商品表 (products)
CREATE TABLE products (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category_id INT NOT NULL COMMENT '分类ID',
    brand VARCHAR(50) COMMENT '品牌',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '售价',
    original_price DECIMAL(10, 2) COMMENT '原价',
    cost_price DECIMAL(10, 2) COMMENT '成本价',
    stock_quantity INT DEFAULT 0 COMMENT '库存数量',
    sold_quantity INT DEFAULT 0 COMMENT '已售数量',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-下架, 1-上架, 2-缺货',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    main_image_url VARCHAR(255) COMMENT '主图URL',
    weight DECIMAL(8, 2) COMMENT '重量(kg)',
    sku VARCHAR(50) COMMENT 'SKU编码',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热销: 0-否, 1-是',
    is_new TINYINT DEFAULT 0 COMMENT '是否新品: 0-否, 1-是',
    rating DECIMAL(2, 1) DEFAULT 5.0 COMMENT '评分(1-5)',
    review_count INT DEFAULT 0 COMMENT '评价数量',
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
) COMMENT='商品表，存储商品信息';

-- 4. 订单表 (orders)
CREATE TABLE orders (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_status TINYINT DEFAULT 0 COMMENT '订单状态: 0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消, 5-退款中',
    total_amount DECIMAL(12, 2) NOT NULL COMMENT '订单总金额',
    discount_amount DECIMAL(12, 2) DEFAULT 0 COMMENT '优惠金额',
    shipping_fee DECIMAL(10, 2) DEFAULT 0 COMMENT '运费',
    pay_amount DECIMAL(12, 2) NOT NULL COMMENT '实付金额',
    pay_time DATETIME COMMENT '支付时间',
    ship_time DATETIME COMMENT '发货时间',
    receive_time DATETIME COMMENT '收货时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    shipping_address VARCHAR(500) COMMENT '收货地址',
    receiver_name VARCHAR(50) COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) COMMENT '收货人电话',
    remark VARCHAR(500) COMMENT '订单备注',
    pay_method TINYINT COMMENT '支付方式: 1-支付宝, 2-微信, 3-银行卡',
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) COMMENT='订单表，存储用户订单信息';

-- 5. 订单商品表 (order_items)
CREATE TABLE order_items (
    item_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单商品ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称（快照）',
    product_image VARCHAR(255) COMMENT '商品图片（快照）',
    price DECIMAL(10, 2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL COMMENT '数量',
    subtotal DECIMAL(12, 2) NOT NULL COMMENT '小计金额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
) COMMENT='订单商品表，存储订单中的商品明细';

-- 创建索引
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_status ON users(status);

CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_created_at ON products(created_at);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(order_status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_pay_time ON orders(pay_time);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

CREATE INDEX idx_categories_parent_id ON categories(parent_id);
