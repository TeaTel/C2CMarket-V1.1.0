package com.campus.backend.service;

import com.campus.backend.dto.OrderCreateDTO;
import com.campus.backend.dto.OrderVO;
import java.util.List;

public interface OrderService {
    
    OrderVO createOrder(OrderCreateDTO createDTO, Long buyerId);
    
    OrderVO getOrderDetail(Long orderId, Long userId);
    
    List<OrderVO> getBuyerOrders(Long buyerId);
    
    List<OrderVO> getSellerOrders(Long sellerId);
    
    void updateOrderStatus(Long orderId, String status, Long userId);
    
    void cancelOrder(Long orderId, Long userId);
    
    void confirmOrder(Long orderId, Long userId);
}