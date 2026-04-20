-- 校园二手交易平台数据库SQL脚本
-- 创建时间：2026-03-18

-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_second_hand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE campus_second_hand;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID，主键，自增长',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，唯一，用于登录',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值，使用BCrypt加密',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号，唯一，用于验证',
    wechat VARCHAR(50) UNIQUE COMMENT '微信号，可选',
    qq VARCHAR(20) UNIQUE COMMENT 'QQ号，可选',
    avatar VARCHAR(255) COMMENT '头像URL地址',
    school VARCHAR(100) COMMENT '学校名称',
    major VARCHAR(100) COMMENT '专业名称',
    is_student BOOLEAN DEFAULT FALSE COMMENT '是否为学生：TRUE-学生，FALSE-教职工',
    status ENUM('active', 'banned') DEFAULT 'active' COMMENT '用户状态：active-正常，banned-封禁',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 创建分类表
CREATE TABLE IF NOT EXISTS categories (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID，主键，自增长',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称，唯一',
    parent_id INT COMMENT '父分类ID，用于构建树形结构，NULL表示顶级分类',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 创建商品表
CREATE TABLE IF NOT EXISTS products (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID，主键，自增长',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品详细描述',
    price DECIMAL(10,2) NOT NULL COMMENT '商品价格，单位：元',
    status ENUM('available', 'sold', 'removed') DEFAULT 'available' COMMENT '商品状态：available-在售，sold-已售出，removed-已下架',
    category_id INT COMMENT '分类ID，外键关联categories表',
    user_id INT COMMENT '卖家用户ID，外键关联users表',
    view_count INT DEFAULT 0 COMMENT '商品浏览次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息表';

-- 创建商品图片表
CREATE TABLE IF NOT EXISTS images (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '图片ID，主键，自增长',
    product_id INT COMMENT '商品ID，外键关联products表',
    url VARCHAR(255) NOT NULL COMMENT '图片URL地址',
    order_index INT DEFAULT 0 COMMENT '图片排序索引，数值越小越靠前',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- 创建订单表
CREATE TABLE IF NOT EXISTS orders (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID，主键，自增长',
    buyer_id INT COMMENT '买家用户ID，外键关联users表',
    seller_id INT COMMENT '卖家用户ID，外键关联users表',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额，单位：元',
    status ENUM('pending', 'paid', 'shipped', 'completed', 'cancelled', 'refunded') DEFAULT 'pending' COMMENT '订单状态：pending-待支付，paid-已支付，shipped-已发货，completed-已完成，cancelled-已取消，refunded-已退款',
    payment_method ENUM('platform', 'offline') COMMENT '支付方式：platform-平台支付，offline-线下支付',
    address VARCHAR(255) COMMENT '收货地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单信息表';

-- 创建订单项表
CREATE TABLE IF NOT EXISTS order_items (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单项ID，主键，自增长',
    order_id INT COMMENT '订单ID，外键关联orders表',
    product_id INT COMMENT '商品ID，外键关联products表',
    quantity INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    price DECIMAL(10,2) NOT NULL COMMENT '商品单价，单位：元',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表，记录订单中的商品明细';

-- 创建消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID，主键，自增长',
    sender_id INT COMMENT '发送者用户ID，外键关联users表',
    receiver_id INT COMMENT '接收者用户ID，外键关联users表',
    content TEXT COMMENT '消息文本内容',
    image_url VARCHAR(255) COMMENT '消息图片URL地址，可选',
    status ENUM('sent', 'delivered', 'read') DEFAULT 'sent' COMMENT '消息状态：sent-已发送，delivered-已送达，read-已读',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 创建评价表
CREATE TABLE IF NOT EXISTS reviews (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID，主键，自增长',
    order_id INT UNIQUE COMMENT '订单ID，外键关联orders表，唯一约束确保一个订单只能有一个评价',
    buyer_id INT COMMENT '买家用户ID，外键关联users表',
    seller_id INT COMMENT '卖家用户ID，外键关联users表',
    rating INT NOT NULL COMMENT '评分，1-5分',
    comment TEXT COMMENT '评价内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易评价表';

-- 创建索引
-- 用户表索引
CREATE INDEX idx_users_status ON users(status);

-- 商品表索引
CREATE INDEX idx_products_user_id ON products(user_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_created_at ON products(created_at);

-- 图片表索引
CREATE INDEX idx_images_product_id ON images(product_id);
CREATE INDEX idx_images_order_index ON images(order_index);

-- 订单表索引
CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX idx_orders_seller_id ON orders(seller_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- 订单项表索引
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- 消息表索引
CREATE INDEX idx_chat_messages_sender_id ON chat_messages(sender_id);
CREATE INDEX idx_chat_messages_receiver_id ON chat_messages(receiver_id);
CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at);

-- 评价表索引
CREATE INDEX idx_reviews_buyer_id ON reviews(buyer_id);
CREATE INDEX idx_reviews_seller_id ON reviews(seller_id);

-- 插入初始分类数据
INSERT INTO categories (name, parent_id) VALUES
('书籍/教材', NULL),
('电子产品', NULL),
('生活用品', NULL),
('服饰鞋包', NULL),
('其他', NULL),
('教材', 1),
('教辅资料', 1),
('课外书籍', 1),
('手机', 2),
('电脑', 2),
('平板', 2),
('耳机音响', 2),
('家用电器', 3),
('个人护理', 3),
('宿舍用品', 3),
('服装', 4),
('鞋子', 4),
('包包', 4),
('配饰', 4);

-- 插入管理员用户（密码：admin123）
INSERT INTO users (username, password_hash, phone, status, is_student) VALUES
('admin', '$2y$10$e5e4W1eQJ5V9w5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5', '13800138000', 'active', 0);

-- 插入测试用户
INSERT INTO users (username, password_hash, phone, wechat, qq, school, major, is_student, status) VALUES
('test_buyer', '$2y$10$e5e4W1eQJ5V9w5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5', '13800138001', 'test_wechat1', '123456', '北京大学', '计算机科学与技术', 1, 'active'),
('test_seller', '$2y$10$e5e4W1eQJ5V9w5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5c5', '13800138002', 'test_wechat2', '234567', '清华大学', '电子工程', 1, 'active');

-- 插入测试商品
INSERT INTO products (name, description, price, status, category_id, user_id, view_count) VALUES
('全新iPhone 13', '全新未拆封iPhone 13，128GB，蓝色', 4999.00, 'available', 9, 2, 100),
('二手MacBook Pro', '2020款MacBook Pro，13英寸，8GB内存，256GB SSD', 6999.00, 'available', 10, 2, 80),
('大学英语四级词汇书', '新东方四级词汇书，几乎全新', 20.00, 'available', 6, 2, 50),
('无线蓝牙耳机', 'AirPods Pro，使用半年，无损坏', 899.00, 'available', 12, 2, 60),
('宿舍书桌', '木质书桌，8成新，尺寸120*60', 150.00, 'available', 15, 2, 30);

-- 插入测试商品图片
INSERT INTO images (product_id, url, order_index) VALUES
(1, 'https://example.com/iphone13_1.jpg', 0),
(1, 'https://example.com/iphone13_2.jpg', 1),
(2, 'https://example.com/macbook_1.jpg', 0),
(3, 'https://example.com/book_1.jpg', 0),
(4, 'https://example.com/airpods_1.jpg', 0),
(5, 'https://example.com/desk_1.jpg', 0);

-- 插入测试订单
INSERT INTO orders (buyer_id, seller_id, total_amount, status, payment_method, address) VALUES
(1, 2, 4999.00, 'pending', 'platform', '北京市海淀区北京大学宿舍1号楼101'),
(1, 2, 20.00, 'completed', 'offline', '北京市海淀区北京大学宿舍1号楼101');

-- 插入测试订单项
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
(1, 1, 1, 4999.00),
(2, 3, 1, 20.00);

-- 插入测试消息
INSERT INTO chat_messages (sender_id, receiver_id, content, status) VALUES
(1, 2, '你好，这个iPhone 13还在吗？', 'read'),
(2, 1, '在的，全新未拆封', 'read'),
(1, 2, '可以便宜一点吗？', 'delivered');

-- 插入测试评价
INSERT INTO reviews (order_id, buyer_id, seller_id, rating, comment) VALUES
(2, 1, 2, 5, '卖家很靠谱，书的 condition 很好！');

-- 查看数据库状态
SHOW TABLES;

-- 查看数据统计
SELECT 'users' AS table_name, COUNT(*) AS count FROM users UNION
SELECT 'categories' AS table_name, COUNT(*) AS count FROM categories UNION
SELECT 'products' AS table_name, COUNT(*) AS count FROM products UNION
SELECT 'images' AS table_name, COUNT(*) AS count FROM images UNION
SELECT 'orders' AS table_name, COUNT(*) AS count FROM orders UNION
SELECT 'order_items' AS table_name, COUNT(*) AS count FROM order_items UNION
SELECT 'chat_messages' AS table_name, COUNT(*) AS count FROM chat_messages UNION
SELECT 'reviews' AS table_name, COUNT(*) AS count FROM reviews;

-- 数据库初始化完成
SELECT '数据库初始化完成' AS message;
