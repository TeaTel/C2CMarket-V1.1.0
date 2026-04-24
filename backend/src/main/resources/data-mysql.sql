-- ============================================================
-- 校园二手交易平台 (Campus Flea Market) - 测试数据
-- 版本: 2.0 (闲鱼风格)
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------
-- 用户数据
-- -----------------------------------------------------------
INSERT IGNORE INTO users (id, username, password_hash, phone, email, nickname, avatar,
                   gender, school, major, grade, wechat, qq, bio, is_student, status)
VALUES
(1, 'zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138001', 'zhangsan@campus.edu.cn', '张三', 'https://picsum.photos/100/100?random=1',
 1, '清华大学', '计算机科学与技术', '大三', 'zs_wechat', '12345678', '热爱编程，喜欢折腾数码产品', 1, 1),
(2, 'lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138002', 'lisi@campus.edu.cn', '李四', 'https://picsum.photos/100/100?random=2',
 2, '北京大学', '电子信息工程', '大四', 'ls_wechat', '23456789', '电子发烧友，出闲置好物', 1, 1),
(3, 'wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138003', 'wangwu@campus.edu.cn', '王五', 'https://picsum.photos/100/100?random=3',
 1, '复旦大学', '金融学', '研一', 'ww_wechat', '34567890', '毕业清仓，好物低价出', 1, 1),
(4, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13900139000', 'admin@campus.edu.cn', '管理员', NULL,
 NULL, NULL, NULL, NULL, NULL, NULL, '系统管理员', 0, 1);

-- 密码统一为: password (BCrypt加密)

-- -----------------------------------------------------------
-- 分类数据 (树形结构)
-- -----------------------------------------------------------

-- 一级分类
INSERT IGNORE INTO categories (id, name, description, icon_url, parent_id, sort_order, status) VALUES
(1, '电子产品', '手机、电脑、平板、耳机等', 'https://img.icons8.com/color/96/laptop.png', NULL, 1, 1),
(2, '图书教材', '教科书、参考书、小说等', 'https://img.icons8.com/color/96/book.png', NULL, 2, 1),
(3, '生活用品', '日用品、家具、电器等', 'https://img.icons8.com/color/96/home.png', NULL, 3, 1),
(4, '运动户外', '运动装备、健身器材、户外用品', 'https://img.icons8.com/color/96/basketball.png', NULL, 4, 1),
(5, '美妆个护', '护肤品、化妆品、洗护用品', 'https://img.icons8.com/color/96/cosmetics.png', NULL, 5, 1);

-- 二级分类
INSERT IGNORE INTO categories (id, name, description, icon_url, parent_id, sort_order, status) VALUES
(11, '手机', '智能手机、功能机', NULL, 1, 1, 1),
(12, '电脑', '笔记本、台式机、配件', NULL, 1, 2, 1),
(13, '平板/配件', '平板电脑、保护套、手写笔', NULL, 1, 3, 1),
(14, '耳机/音响', '有线/无线耳机、音箱', NULL, 1, 4, 1),

(21, '教材教辅', '大学教材、考研资料', NULL, 2, 1, 1),
(22, '文学小说', '小说、散文、诗歌', NULL, 2, 2, 1),
(23, '考试考证', '四六级、公务员、职业资格', NULL, 2, 3, 1),

(31, '家居日用', '收纳、清洁、小工具', NULL, 3, 1, 1),
(32, '家用电器', '小家电、厨房用具', NULL, 3, 2, 1),
(33, '宿舍神器', '床上用品、桌面整理', NULL, 3, 3, 1);

-- -----------------------------------------------------------
-- 商品数据 (闲鱼风格 - 含成色、原价、多图)
-- -----------------------------------------------------------
INSERT IGNORE INTO products (id, name, description, price, original_price, category_id, seller_id,
                      condition_level, image_urls, cover_image, status, view_count, like_count,
                      location, delivery_method) VALUES

-- 电子产品类
(1, 'iPhone 14 Pro Max 256G 暗紫色', '自用iPhone 14 Pro Max，256G暗紫色，电池健康度95%，无磕碰划痕，全套原装配件都在。因为换了15所以出，价格可小刀。支持面交验机。',
 6500.00, 9999.00, 11, 1, 2,
 '["https://picsum.photos/800/600?random=101","https://picsum.photos/800/600?random=102","https://picsum.photos/800/600?random=103"]',
 'https://picsum.photos/400/300?random=101', 1, 156, 23, '清华西门', 1),

(2, 'MacBook Air M2 16+512 星光色', '2023款MacBook Air M2芯片，16G内存+512G存储，星光色。屏幕完美无亮点，键盘无油光，电池循环次数仅80次。送原装充电器和内胆包。',
 8500.00, 11999.00, 12, 1, 2,
 '["https://picsum.photos/800/600?random=201","https://picsum.photos/800/600?random=202"]',
 'https://picsum.photos/400/300?random=201', 1, 89, 15, '北大东门', 1),

(3, 'Sony WH-1000XM5 头戴式降噪耳机', '索尼旗舰降噪耳机，黑色。降噪效果一流，音质出色，续航约30小时。成色99新，只有轻微使用痕迹。包装盒配件齐全。',
 1800.00, 2999.00, 14, 1, 3,
 '["https://picsum.photos/800/600?random=301","https://picsum.photos/800/600?random=302","https://picsum.photos/800/600?random=303"]',
 'https://picsum.photos/400/300?random=301', 1, 67, 8, '复旦本部', 2),

(4, 'iPad Pro 11寸 M2 128G WiFi版', 'iPad Pro 2022款 M2芯片，11寸深空灰，128G。配Apple Pencil二代和妙控键盘（需另外购买）。屏幕贴膜带壳使用，完美无瑕。',
 5500.00, 6999.00, 13, 2, 1,
 '["https://picsum.photos/800/600?random=401","https://picsum.photos/800/600?random=402"]',
 'https://picsum.photos/400/300?random=401', 1, 203, 45, '清华图书馆', 1),

-- 图书教材类
(5, '高等数学 同济第七版 上下册', '同济大学《高等数学》第七版上下册，几乎全新，只有少量铅笔笔记。配套习题解答也有，打包出售。',
 35.00, 78.00, 21, 1, 1,
 '["https://picsum.photos/800/600?random=501"]',
 'https://picsum.photos/400/300?random=501', 1, 312, 56, '北大教学楼', 3),

(6, 'CSAPP 深入理解计算机系统 第三版', 'Computer Systems: A Programmer\'s Perspective 第三版英文原版，经典计算机系统教材。书脊有折痕但内容完好，适合计科同学。',
 55.00, 139.00, 21, 2, 3,
 '["https://picsum.photos/800/600?random=601","https://picsum.photos/800/600?random=602"]',
 'https://picsum.photos/400/300?random=601', 1, 178, 34, '清华信息楼', 2),

(7, '三体 全套三册 刘慈欣', '三体全集：三体、黑暗森林、死神永生。雨果奖获奖作品，科幻迷必读。正版精装版，品相良好。',
 28.00, 88.00, 22, 3, 2,
 '["https://picsum.photos/800/600?random=701","https://picsum.photos/800/600?random=702","https://picsum.photos/800/600?random=703"]',
 'https://picsum.photos/400/300?random=701', 1, 445, 78, '复旦文科楼', 3),

-- 生活用品类
(8, '小米空气净化器4 Lite', '小米空气净化器4 Lite，白色。除甲醛PM2.5效果不错，宿舍/小房间够用。用了半年，滤芯还有80%寿命。',
 299.00, 599.00, 32, 2, 3,
 '["https://picsum.photos/800/600?random=801","https://picsum.photos/800/600?random=802"]',
 'https://picsum.photos/400/300?random=801', 1, 124, 19, '北大宿舍区', 2),

(9, '宜家落地灯 简约北欧风', '宜家HEKTAR系列落地灯，暖黄光，高度可调。搬家带不走所以出，九成新，无任何损坏。',
 85.00, 199.00, 31, 3, 2,
 '["https://picsum.photos/800/600?random=901"]',
 'https://picsum.photos/400/300?random=901', 1, 56, 7, '清华紫荆公寓', 3),

-- 运动户外类
(10, '迪卡侬入门级公路车', '迪卡侬Triban RC120公路自行车，铝合金车架，Shimano Claris套件。骑行不到500公里，保养到位，链条上过油。',
 1200.00, 2299.00, 4, 1, 3,
 '["https://picsum.photos/800/600?random=1001","https://picsum.photos/800/600?random=1002","https://picsum.photos/800/600?random=1003"]',
 'https://picsum.photos/400/300?random=1001', 1, 98, 12, '北大体育馆', 2),

-- 已售出商品示例
(11, 'Kindle Paperwhite 5 8G 黑色', '亚马逊Kindle Paperwhite第5代，8G存储，黑色。带保护套和充电器。已出勿念。',
 680.00, 1068.00, 14, 1, 1,
 '["https://picsum.photos/800/600?random=1101"]',
 'https://picsum.photos/400/300?random=1101', 2, 334, 42, '北大图书馆', 1);

-- -----------------------------------------------------------
-- 收藏数据
-- -----------------------------------------------------------
INSERT IGNORE INTO favorites (user_id, product_id, created_at) VALUES
(2, 1, NOW()),
(2, 4, NOW()),
(3, 1, NOW()),
(3, 2, NOW()),
(3, 6, NOW()),
(1, 10, NOW()),
(1, 7, NOW());

-- -----------------------------------------------------------
-- 聊天会话数据 (示例)
-- -----------------------------------------------------------
INSERT IGNORE INTO chat_conversations (id, user1_id, user2_id, product_id, last_message, last_message_at,
                                 user1_unread, user2_unread, status) VALUES
(1, 1, 2, 1, '这个iPhone能再便宜点吗？我预算6000左右', NOW() - INTERVAL 2 HOUR, 0, 1, 1),
(2, 1, 3, 2, 'MacBook还在吗？可以看看实物吗？', NOW() - INTERVAL 30 MINUTE, 0, 0, 1),
(3, 2, 3, NULL, '你好，想问问你那边的书还有没有？', NOW() - INTERVAL 1 DAY, 1, 0, 1);

-- -----------------------------------------------------------
-- 聊天消息数据 (示例)
-- -----------------------------------------------------------
INSERT IGNORE INTO chat_messages (conversation_id, sender_id, receiver_id, content, message_type, is_read) VALUES
-- 会话1: 张三和李四关于iPhone的对话
(1, 2, 1, '你好！这个iPhone 14 Pro Max还在吗？', 'text', 1),
(1, 1, 2, '在的在的，刚发布不久呢', 'text', 1),
(1, 2, 1, '能详细说说使用情况吗？电池健康度多少？', 'text', 1),
(1, 1, 2, '电池健康度95%，平时很爱惜的，一直戴壳贴膜使用', 'text', 1),
(1, 2, 1, '这个iPhone能再便宜点吗？我预算6000左右', 'text', 0),

-- 会话2: 张三和王五关于MacBook的对话
(2, 3, 1, '你好，看到你在出MacBook Air M2', 'text', 1),
(2, 1, 3, '是的，需要了解什么可以问', 'text', 1),
(2, 3, 1, 'MacBook还在吗？可以看看实物吗？', 'text', 1),

-- 会话3: 李四和王五关于书的对话
(3, 3, 2, '你好，看到你出了不少好书', 'text', 1),
(3, 2, 3, '是的，毕业清仓中~', 'text', 0),
(3, 3, 2, '你好，想问问你那边的书还有没有？', 'text', 0);

-- -----------------------------------------------------------
-- 订单数据 (不含支付)
-- -----------------------------------------------------------
INSERT IGNORE INTO orders (id, order_no, buyer_id, seller_id, total_amount, status, remark, address, created_at) VALUES
(1, 'ORD2026042300001', 2, 1, 6500.00, 'pending', '希望能便宜100块，面交时验机', '清华西门地铁站', NOW() - INTERVAL 3 HOUR),
(2, 'ORD2026042300002', 3, 1, 5500.00, 'completed', '', '清华图书馆门口', NOW() - INTERVAL 1 DAY),
(3, 'ORD2026042300003', 1, 2, 1800.00, 'cancelled', '临时有事不买了', '', NOW() - INTERVAL 2 DAY);

-- 订单项
INSERT IGNORE INTO order_items (order_id, product_id, product_name, product_image, price, quantity, subtotal) VALUES
(1, 1, 'iPhone 14 Pro Max 256G 暗紫色', 'https://picsum.photos/400/300?random=101', 6500.00, 1, 6500.00),
(2, 4, 'iPad Pro 11寸 M2 128G WiFi版', 'https://picsum.photos/400/300?random=401', 5500.00, 1, 5500.00),
(3, 3, 'Sony WH-1000XM5 头戴式降噪耳机', 'https://picsum.photos/400/300?random=301', 1800.00, 1, 1800.00);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 完成! 共:
--   4 个用户 (含1管理员)
--   14 个分类 (5一级 + 9二级)
--   11 个商品 (10在售 + 1已售)
--   7 条收藏记录
--   3 个聊天会话 + 11 条消息
--   3 个订单 (1待确认 + 1已完成 + 1已取消)
-- ============================================================
