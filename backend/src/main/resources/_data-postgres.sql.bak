-- PostgreSQL 测试数据初始化脚本
-- 校园二手交易平台测试数据

-- 插入测试用户 (密码都是: password123)
INSERT INTO users (username, password, email, phone, role) VALUES
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwE.', 'student1@example.com', '13800138001', 'STUDENT'),
('student2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwE.', 'student2@example.com', '13800138002', 'STUDENT'),
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwE.', 'teacher1@example.com', '13800138003', 'TEACHER')
ON CONFLICT (username) DO NOTHING;

-- 插入商品分类
INSERT INTO categories (name, description, parent_id) VALUES
('电子产品', '手机、电脑、平板等电子设备', NULL),
('学习资料', '教材、笔记、参考书等学习材料', NULL),
('生活用品', '日常生活中的各种用品', NULL),
('交通工具', '自行车、电动车等交通工具', NULL),
('其他', '其他类型的商品', NULL)
ON CONFLICT DO NOTHING;

-- 插入子分类
INSERT INTO categories (name, description, parent_id) VALUES
('手机', '智能手机、功能手机', 1),
('电脑', '笔记本电脑、台式机', 1),
('平板', 'iPad、安卓平板', 1),
('教材', '各学科教材', 2),
('笔记', '学习笔记、复习资料', 2),
('家具', '桌椅、床等家具', 3),
('电器', '小家电、厨房电器', 3),
('自行车', '山地车、公路车', 4),
('电动车', '电动自行车、电动滑板车', 4)
ON CONFLICT DO NOTHING;

-- 插入测试商品
INSERT INTO products (title, description, price, category_id, seller_id, status, location, contact_phone) VALUES
('iPhone 14 Pro 256G', '几乎全新，使用半年，无划痕，原装配件齐全', 5999.00, 6, 1, 'AVAILABLE', '学生宿舍A栋', '13800138001'),
('Java编程思想 第5版', '学习Java的经典教材，有少量笔记', 45.00, 9, 2, 'AVAILABLE', '图书馆附近', '13800138002'),
('联想拯救者Y7000', '游戏本，i7处理器，16G内存，512G SSD', 4500.00, 7, 1, 'AVAILABLE', '计算机学院', '13800138001'),
('高等数学笔记', '大一高等数学完整笔记，重点突出', 20.00, 10, 2, 'AVAILABLE', '数学系', '13800138002'),
('山地自行车', '捷安特山地车，21速，车况良好', 800.00, 13, 1, 'AVAILABLE', '体育馆', '13800138001'),
('小米电动滑板车', '续航30公里，可折叠，带发票', 1200.00, 14, 2, 'AVAILABLE', '校门口', '13800138002'),
('台灯', 'LED护眼台灯，三档调光', 50.00, 12, 1, 'AVAILABLE', '学生宿舍B栋', '13800138001'),
('英语四级词汇书', '全新未使用，附带记忆卡片', 25.00, 9, 2, 'AVAILABLE', '外语学院', '13800138002')
ON CONFLICT DO NOTHING;

-- 插入测试订单
INSERT INTO orders (product_id, buyer_id, seller_id, price, status, meeting_location, meeting_time) VALUES
(1, 2, 1, 5999.00, 'COMPLETED', '学生宿舍A栋门口', '2026-04-15 14:00:00'),
(3, 2, 1, 4500.00, 'PENDING', '计算机学院大厅', '2026-04-20 16:00:00'),
(5, 3, 1, 800.00, 'CONFIRMED', '体育馆东门', '2026-04-18 10:00:00')
ON CONFLICT DO NOTHING;

-- 插入测试消息
INSERT INTO messages (sender_id, receiver_id, content, is_read) VALUES
(2, 1, '你好，我对你的iPhone感兴趣，能便宜点吗？', true),
(1, 2, '最低5800，不能再少了', true),
(2, 1, '好的，我考虑一下', false),
(3, 1, '自行车还能骑吗？有没有什么问题？', true),
(1, 3, '车况很好，刚做过保养', false)
ON CONFLICT DO NOTHING;

-- 插入测试收藏
INSERT INTO favorites (user_id, product_id) VALUES
(2, 1),
(2, 3),
(3, 5),
(1, 2)
ON CONFLICT (user_id, product_id) DO NOTHING;