package com.campus.backend.common;

public enum ErrorCode {
    
    // 通用错误码
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    
    // 业务错误码 (1000-1999)
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    INVALID_CREDENTIALS(1003, "用户名或密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),
    INVALID_TOKEN(1005, "无效的令牌"),
    TOKEN_EXPIRED(1006, "令牌已过期"),
    
    // 商品相关错误码 (2000-2999)
    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    PRODUCT_UNAVAILABLE(2002, "商品不可用"),
    INSUFFICIENT_STOCK(2003, "库存不足"),
    PRODUCT_OWNERSHIP_ERROR(2004, "无权操作此商品"),
    
    // 订单相关错误码 (3000-3999)
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态错误"),
    PAYMENT_FAILED(3003, "支付失败"),
    
    // 分类相关错误码 (4000-4999)
    CATEGORY_NOT_FOUND(4001, "分类不存在"),
    CATEGORY_HAS_CHILDREN(4002, "分类包含子分类，无法删除"),
    
    // 消息相关错误码 (5000-5999)
    MESSAGE_NOT_FOUND(5001, "消息不存在"),
    
    // 验证错误码 (6000-6999)
    VALIDATION_ERROR(6001, "数据验证失败"),
    
    // 系统错误码 (9000-9999)
    DATABASE_ERROR(9001, "数据库操作失败"),
    EXTERNAL_SERVICE_ERROR(9002, "外部服务调用失败"),
    UNKNOWN_ERROR(9999, "未知错误");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return UNKNOWN_ERROR;
    }
}