-- ============================================================
-- 校园二手交易平台 (Campus Flea Market) - MySQL数据库表结构
-- 版本: 2.0 (闲鱼风格全面重构)
-- ============================================================

-- -----------------------------------------------------------
-- 1. 用户表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username        VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password_hash   VARCHAR(255) NOT NULL COMMENT '密码哈希(BCrypt)',
    phone           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    email           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    nickname        VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    avatar          VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    gender          TINYINT(1)   DEFAULT NULL COMMENT '性别:0未知,1男,2女',
    school          VARCHAR(100) DEFAULT NULL COMMENT '学校名称',
    major           VARCHAR(100) DEFAULT NULL COMMENT '专业',
    grade           VARCHAR(20)  DEFAULT NULL COMMENT '年级',
    wechat          VARCHAR(50)  DEFAULT NULL COMMENT '微信号',
    qq              VARCHAR(20)  DEFAULT NULL COMMENT 'QQ号',
    bio             VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    is_student      TINYINT(1)   DEFAULT 1 COMMENT '是否在校学生',
    status          TINYINT(1)   DEFAULT 1 COMMENT '状态:0禁用,1正常,2封禁',
    last_login_at   DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_school (school)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------------
-- 2. 商品分类表 (树形结构)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name            VARCHAR(50)  NOT NULL COMMENT '分类名称',
    description     VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
    icon_url        VARCHAR(255) DEFAULT NULL COMMENT '图标URL',
    parent_id       BIGINT       DEFAULT NULL COMMENT '父分类ID(NULL表示根分类)',
    sort_order      INT          DEFAULT 0 COMMENT '排序序号(越小越靠前)',
    status          TINYINT(1)   DEFAULT 1 COMMENT '状态:0禁用,1启用',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- -----------------------------------------------------------
-- 3. 商品表 (优化版 - 增加成色/原价等闲鱼风格字段)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    name            VARCHAR(255) NOT NULL COMMENT '商品标题',
    description     TEXT         DEFAULT NULL COMMENT '商品详细描述',
    price           DECIMAL(10,2) NOT NULL COMMENT '现售价格',
    original_price  DECIMAL(10,2) DEFAULT NULL COMMENT '原价/全新价格',
    category_id     BIGINT       DEFAULT NULL COMMENT '所属分类ID',
    seller_id       BIGINT       NOT NULL COMMENT '卖家用户ID',
    condition_level TINYINT      DEFAULT NULL COMMENT '成色等级:1全新,2几乎全新,3轻微使用痕迹,4明显使用痕迹,5一般',
    image_urls      JSON         DEFAULT NULL COMMENT '商品图片URL列表(JSON数组)',
    cover_image     VARCHAR(255) DEFAULT NULL COMMENT '封面图片URL',
    status          TINYINT(1)   DEFAULT 1 COMMENT '状态:0已下架/删除,1在售,2已售出,3预约中',
    view_count      INT          DEFAULT 0 COMMENT '浏览次数',
    like_count      INT          DEFAULT 0 COMMENT '收藏/喜欢次数',
    location        VARCHAR(100) DEFAULT NULL COMMENT '交易地点(如:清华图书馆)',
    delivery_method TINYINT(1)   DEFAULT NULL COMMENT '交付方式:1自提,2快递,3均可',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_category_id (category_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_price (price),
    INDEX idx_created_at (created_at),
    FULLTEXT INDEX ft_name_desc (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- -----------------------------------------------------------
-- 4. 聊天会话表 (核心新功能 - 用于私信系统)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_conversations (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    user1_id        BIGINT       NOT NULL COMMENT '参与者1的用户ID',
    user2_id        BIGINT       NOT NULL COMMENT '参与者2的用户ID',
    product_id      BIGINT       DEFAULT NULL COMMENT '关联的商品ID(可选)',
    last_message    TEXT         DEFAULT NULL COMMENT '最后一条消息内容',
    last_message_at DATETIME     DEFAULT NULL COMMENT '最后消息时间',
    user1_unread    INT          DEFAULT 0 COMMENT 'user1的未读消息数',
    user2_unread    INT          DEFAULT 0 COMMENT 'user2的未读消息数',
    status          TINYINT(1)   DEFAULT 1 COMMENT '状态:0已关闭,1正常',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
    UNIQUE INDEX uk_user_pair (user1_id, user2_id),
    INDEX idx_user1 (user1_id),
    INDEX idx_user2 (user2_id),
    INDEX idx_product (product_id),
    INDEX idx_last_message_at (last_message_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- -----------------------------------------------------------
-- 5. 聊天消息表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_messages (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    conversation_id BIGINT       NOT NULL COMMENT '所属会话ID',
    sender_id       BIGINT       NOT NULL COMMENT '发送者用户ID',
    receiver_id     BIGINT       NOT NULL COMMENT '接收者用户ID',
    content         TEXT         NOT NULL COMMENT '消息内容',
    message_type    VARCHAR(20)  DEFAULT 'text' COMMENT '消息类型:text,image,system',
    is_read         TINYINT(1)   DEFAULT 0 COMMENT '是否已读:0未读,1已读',
    read_at         DATETIME     DEFAULT NULL COMMENT '阅读时间',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- -----------------------------------------------------------
-- 6. 订单表 (简化版 - 不含支付功能)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no        VARCHAR(32)  NOT NULL UNIQUE COMMENT '订单编号',
    buyer_id        BIGINT       NOT NULL COMMENT '买家用户ID',
    seller_id       BIGINT       NOT NULL COMMENT '卖家用户ID',
    total_amount    DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status          VARCHAR(20)  DEFAULT 'pending' COMMENT '订单状态:pending待确认,cancelled已取消,completed已完成,refunded已退款',
    remark          VARCHAR(255) DEFAULT NULL COMMENT '买家备注',
    address         VARCHAR(255) DEFAULT NULL COMMENT '交易/收货地址',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    completed_at    DATETIME     DEFAULT NULL COMMENT '完成时间',
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_status (status),
    INDEX idx_order_no (order_no),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- -----------------------------------------------------------
-- 7. 订单项表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID',
    order_id        BIGINT       NOT NULL COMMENT '所属订单ID',
    product_id      BIGINT       DEFAULT NULL COMMENT '商品ID',
    product_name    VARCHAR(255) NOT NULL COMMENT '商品名称(快照)',
    product_image   VARCHAR(255) DEFAULT NULL COMMENT '商品图片(快照)',
    price           DECIMAL(10,2) NOT NULL COMMENT '下单时单价(快照)',
    quantity        INT          NOT NULL DEFAULT 1 COMMENT '数量',
    subtotal        DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';

-- -----------------------------------------------------------
-- 8. 商品收藏表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS favorites (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '收藏ID',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    product_id      BIGINT       NOT NULL COMMENT '商品ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE INDEX uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品收藏表';
