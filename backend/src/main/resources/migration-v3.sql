-- ============================================================
-- 数据库结构修复脚本 - v3
-- 用途：为已有数据库添加缺失的字段
-- 说明：使用 ALTER TABLE ADD COLUMN IF NOT EXISTS 确保幂等性
-- ============================================================

-- -----------------------------------------------------------
-- 1. 修复 users 表 - 添加缺失字段
-- -----------------------------------------------------------
-- nickname 字段（用户注册/查询必需）
ALTER TABLE users ADD COLUMN IF NOT EXISTS nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称';
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL';
ALTER TABLE users ADD COLUMN IF NOT EXISTS gender TINYINT(1) DEFAULT NULL COMMENT '性别:0未知,1男,2女';
ALTER TABLE users ADD COLUMN IF NOT EXISTS school VARCHAR(100) DEFAULT NULL COMMENT '学校名称';
ALTER TABLE users ADD COLUMN IF NOT EXISTS major VARCHAR(100) DEFAULT NULL COMMENT '专业';
ALTER TABLE users ADD COLUMN IF NOT EXISTS grade VARCHAR(20) DEFAULT NULL COMMENT '年级';
ALTER TABLE users ADD COLUMN IF NOT EXISTS wechat VARCHAR(50) DEFAULT NULL COMMENT '微信号';
ALTER TABLE users ADD COLUMN IF NOT EXISTS qq VARCHAR(20) DEFAULT NULL COMMENT 'QQ号';
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio VARCHAR(255) DEFAULT NULL COMMENT '个人简介';
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_student TINYINT(1) DEFAULT 1 COMMENT '是否在校学生';
ALTER TABLE users ADD COLUMN IF NOT EXISTS status TINYINT(1) DEFAULT 1 COMMENT '状态:0禁用,1正常,2封禁';
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间';
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 修复 phone/email 约束（如果是已有表可能有旧数据）
ALTER TABLE users MODIFY COLUMN phone VARCHAR(20) DEFAULT NULL COMMENT '手机号';
ALTER TABLE users MODIFY COLUMN email VARCHAR(100) DEFAULT NULL COMMENT '邮箱';

-- 添加索引（如果不存在）
ALTER TABLE users ADD INDEX IF NOT EXISTS idx_username (username);
ALTER TABLE users ADD INDEX IF NOT EXISTS idx_phone (phone);
ALTER TABLE users ADD INDEX IF NOT EXISTS idx_status (status);
ALTER TABLE users ADD INDEX IF NOT EXISTS idx_school (school);

-- -----------------------------------------------------------
-- 2. 修复 categories 表
-- -----------------------------------------------------------
ALTER TABLE categories ADD COLUMN IF NOT EXISTS description VARCHAR(255) DEFAULT NULL COMMENT '分类描述';
ALTER TABLE categories ADD COLUMN IF NOT EXISTS icon_url VARCHAR(255) DEFAULT NULL COMMENT '图标URL';
ALTER TABLE categories ADD COLUMN IF NOT EXISTS parent_id BIGINT DEFAULT NULL COMMENT '父分类ID(NULL表示一级分类)';
ALTER TABLE categories ADD COLUMN IF NOT EXISTS sort_order INT DEFAULT 0 COMMENT '排序序号';
ALTER TABLE categories ADD COLUMN IF NOT EXISTS status TINYINT(1) DEFAULT 1 COMMENT '状态:0禁用,1正常';
ALTER TABLE categories ADD COLUMN IF NOT EXISTS updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- -----------------------------------------------------------
-- 3. 修复 products 表
-- -----------------------------------------------------------
ALTER TABLE products ADD COLUMN IF NOT EXISTS original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价/全新价格';
ALTER TABLE products ADD COLUMN IF NOT EXISTS condition_level TINYINT DEFAULT NULL COMMENT '成色:1全新,2几乎新,3轻微使用,4明显使用,5一般';
ALTER TABLE products ADD COLUMN IF NOT EXISTS image_urls JSON DEFAULT NULL COMMENT '图片URL列表(JSON数组)';
ALTER TABLE products ADD COLUMN IF NOT EXISTS cover_image VARCHAR(255) DEFAULT NULL COMMENT '封面图URL';
ALTER TABLE products ADD COLUMN IF NOT EXISTS status TINYINT(1) DEFAULT 1 COMMENT '状态:0下架/已售,1在售,2审核中';
ALTER TABLE products ADD COLUMN IF NOT EXISTS view_count INT DEFAULT 0 COMMENT '浏览次数';
ALTER TABLE products ADD COLUMN IF NOT EXISTS like_count INT DEFAULT 0 COMMENT '收藏次数';
ALTER TABLE products ADD COLUMN IF NOT EXISTS location VARCHAR(100) DEFAULT NULL COMMENT '交易地点';
ALTER TABLE products ADD COLUMN IF NOT EXISTS delivery_method TINYINT(1) DEFAULT NULL COMMENT '交付方式:1自提,2快递,3均可';
ALTER TABLE products ADD COLUMN IF NOT EXISTS updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- -----------------------------------------------------------
-- 4. 修复 orders 表
-- -----------------------------------------------------------
ALTER TABLE orders ADD COLUMN IF NOT EXISTS address VARCHAR(255) DEFAULT NULL COMMENT '交易地点/收货地址';
ALTER TABLE orders ADD COLUMN IF NOT EXISTS completed_at DATETIME DEFAULT NULL COMMENT '完成时间';
ALTER TABLE orders ADD COLUMN IF NOT EXISTS updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- -----------------------------------------------------------
-- 5. 创建 chat_conversations 表（如果不存在）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_conversations (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user1_id        BIGINT       NOT NULL,
    user2_id        BIGINT       NOT NULL,
    product_id      BIGINT       DEFAULT NULL,
    last_message    TEXT         DEFAULT NULL,
    last_message_at DATETIME     DEFAULT NULL,
    user1_unread    INT          DEFAULT 0,
    user2_unread    INT          DEFAULT 0,
    status          TINYINT(1)   DEFAULT 1,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
    INDEX idx_user1 (user1_id),
    INDEX idx_user2 (user2_id),
    INDEX idx_product (product_id),
    INDEX idx_last_message_at (last_message_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- -----------------------------------------------------------
-- 6. 创建 chat_messages 表（如果不存在）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_messages (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT       NOT NULL,
    sender_id       BIGINT       NOT NULL,
    receiver_id     BIGINT       NOT NULL,
    content         TEXT         NOT NULL,
    message_type    VARCHAR(20)  DEFAULT 'text',
    is_read         TINYINT(1)   DEFAULT 0,
    read_at         DATETIME     DEFAULT NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- -----------------------------------------------------------
-- 7. 创建 favorites 表（如果不存在）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS favorites (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    product_id      BIGINT       NOT NULL,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE INDEX uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- -----------------------------------------------------------
-- 8. 创建 order_items 表（如果不存在）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT       NOT NULL COMMENT '订单ID',
    product_id      BIGINT       NOT NULL COMMENT '商品ID',
    product_name    VARCHAR(200) NOT NULL COMMENT '商品名称(快照)',
    product_image   VARCHAR(255) DEFAULT NULL COMMENT '商品图片(快照)',
    price           DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity        INT          DEFAULT 1 COMMENT '数量',
    subtotal        DECIMAL(10,2) NOT NULL COMMENT '小计',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';
