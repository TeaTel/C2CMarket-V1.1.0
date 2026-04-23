-- 创建users表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    wechat VARCHAR(50),
    qq VARCHAR(20),
    avatar VARCHAR(255),
    school VARCHAR(100),
    major VARCHAR(100),
    is_student BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建categories表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    parent_id BIGINT,
    icon_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- 创建products表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category_id BIGINT,
    seller_id BIGINT,
    status INT DEFAULT 1,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 插入测试用户数据
INSERT INTO users (username, password_hash, phone, email, school, major, is_student) VALUES
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwE.', '13800138001', 'student1@example.com', '清华大学', '计算机科学', TRUE),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwE.', '13800138002', 'student2@example.com', '北京大学', '软件工程', TRUE),
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwE.', '13800138003', 'teacher1@example.com', '清华大学', '计算机学院', FALSE);

-- 插入测试分类数据
INSERT INTO categories (name, description, sort_order, status, parent_id, icon_url) VALUES
('电子产品', '手机、电脑、平板等电子设备', 1, 1, NULL, '/icons/electronics.png'),
('书籍教材', '教科书、参考书、小说等', 2, 1, NULL, '/icons/books.png'),
('生活用品', '日常生活中的各种用品', 3, 1, NULL, '/icons/daily.png'),
('服装鞋帽', '衣服、鞋子、帽子等', 4, 1, NULL, '/icons/clothing.png'),
('运动器材', '体育用品和健身器材', 5, 1, NULL, '/icons/sports.png'),
('其他', '其他未分类的物品', 6, 1, NULL, '/icons/other.png'),
('手机', '智能手机、功能手机', 1, 1, 1, '/icons/phone.png'),
('笔记本电脑', '便携式电脑', 2, 1, 1, '/icons/laptop.png'),
('教科书', '学校课程教材', 1, 1, 2, '/icons/textbook.png'),
('小说', '文学作品', 2, 1, 2, '/icons/novel.png');

-- 插入测试商品数据
INSERT INTO products (name, description, price, category_id, seller_id, status, image_url) VALUES
('二手笔记本电脑', '联想ThinkPad X1 Carbon，i7处理器，16GB内存，512GB SSD，9成新', 3500.00, 8, 1, 1, '/images/laptop1.jpg'),
('大学物理教材', '全新未使用，包含习题解答，适合大学物理课程', 50.00, 9, 2, 1, '/images/physics_book.jpg'),
('山地自行车', '捷安特山地自行车，27速，9成新，带锁和头盔', 800.00, 5, 3, 1, '/images/bike.jpg'),
('索尼无线降噪耳机', 'WH-1000XM4，音质优秀，降噪效果好', 1200.00, 1, 1, 1, '/images/headphone.jpg'),
('卡西欧科学计算器', 'fx-991CN X，考试必备，功能齐全', 150.00, 1, 2, 1, '/images/calculator.jpg'),
('iPhone 13 Pro', '256GB，深空灰，95新，原装配件齐全', 4500.00, 7, 3, 1, '/images/iphone.jpg'),
('Java编程思想', '第5版，经典编程书籍，几乎全新', 80.00, 10, 1, 1, '/images/java_book.jpg'),
('冬季羽绒服', '波司登羽绒服，L码，保暖性好，只穿过几次', 300.00, 4, 2, 1, '/images/coat.jpg');

-- 创建orders表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    payment_method VARCHAR(50),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建order_items表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建messages表
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    product_id BIGINT,
    message_type VARCHAR(20) DEFAULT 'text',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入测试订单数据
INSERT INTO orders (buyer_id, seller_id, total_amount, status, payment_method, address) VALUES
(1, 2, 50.00, 'completed', 'wechat', '清华大学学生宿舍1号楼'),
(2, 1, 3500.00, 'pending', 'alipay', '北京大学学生宿舍2号楼');

-- 插入测试订单项数据
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
(1, 2, 1, 50.00),  -- 大学物理教材
(2, 1, 1, 3500.00); -- 二手笔记本电脑

-- 插入测试消息数据
INSERT INTO messages (sender_id, receiver_id, content, product_id, message_type) VALUES
(1, 2, '你好，我对你的大学物理教材感兴趣，请问还在吗？', 2, 'text'),
(2, 1, '还在的，你可以随时来取', 2, 'text'),
(2, 1, '你好，笔记本电脑还能便宜点吗？', 1, 'text'),
(1, 2, '最低3300，不能再低了', 1, 'text');