package com.campus.backend.dto;

import java.time.LocalDateTime;

public class MessageVO {
    private Integer id;
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private Integer productId;
    private String messageType;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private UserVO senderInfo;
    private UserVO receiverInfo;
    private ProductVO productInfo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserVO getSenderInfo() {
        return senderInfo;
    }

    public void setSenderInfo(UserVO senderInfo) {
        this.senderInfo = senderInfo;
    }

    public UserVO getReceiverInfo() {
        return receiverInfo;
    }

    public void setReceiverInfo(UserVO receiverInfo) {
        this.receiverInfo = receiverInfo;
    }

    public ProductVO getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(ProductVO productInfo) {
        this.productInfo = productInfo;
    }
}