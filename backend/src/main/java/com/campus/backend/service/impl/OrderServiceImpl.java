package com.campus.backend.service.impl;

import com.campus.backend.dto.OrderCreateDTO;
import com.campus.backend.dto.OrderVO;
import com.campus.backend.entity.Order;
import com.campus.backend.entity.OrderItem;
import com.campus.backend.entity.Product;
import com.campus.backend.mapper.OrderMapper;
import com.campus.backend.mapper.ProductMapper;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public OrderVO createOrder(OrderCreateDTO createDTO, Long buyerId) {
        // 创建订单
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setSellerId(createDTO.getSellerId());
        order.setPaymentMethod(createDTO.getPaymentMethod());
        order.setAddress(createDTO.getAddress());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        // 计算总金额并创建订单项
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderCreateDTO.OrderItemDTO itemDTO : createDTO.getItems()) {
            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在: " + itemDTO.getProductId());
            }
            
            if (!"active".equals(product.getStatus())) {
                throw new RuntimeException("商品不可用: " + product.getName());
            }
            
            // 检查商品是否属于指定卖家
            if (!product.getSellerId().equals(createDTO.getSellerId())) {
                throw new RuntimeException("商品不属于指定卖家");
            }
            
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(itemDTO.getProductId());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItems.add(orderItem);
        }
        
        order.setTotalAmount(totalAmount);
        
        // 保存订单
        orderMapper.insert(order);
        
        // 保存订单项
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderMapper.insertOrderItem(orderItem);
        }
        
        return convertToVO(order);
    }

    @Override
    public OrderVO getOrderDetail(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 检查权限：只有买家或卖家可以查看订单
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new RuntimeException("无权查看此订单");
        }
        
        return convertToVO(order);
    }

    @Override
    public List<OrderVO> getBuyerOrders(Long buyerId) {
        List<Order> orders = orderMapper.selectByBuyerId(buyerId);
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<OrderVO> getSellerOrders(Long sellerId) {
        List<Order> orders = orderMapper.selectBySellerId(sellerId);
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String status, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 检查权限：只有卖家可以更新订单状态
        if (!order.getSellerId().equals(userId)) {
            throw new RuntimeException("无权更新此订单状态");
        }
        
        orderMapper.updateStatus(orderId, status);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 检查权限：只有买家可以取消订单
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权取消此订单");
        }
        
        // 只能取消待处理的订单
        if (!"pending".equals(order.getStatus())) {
            throw new RuntimeException("只能取消待处理的订单");
        }
        
        orderMapper.updateStatus(orderId, "cancelled");
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 检查权限：只有买家可以确认收货
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权确认此订单");
        }
        
        // 只能确认已发货的订单
        if (!"shipped".equals(order.getStatus())) {
            throw new RuntimeException("只能确认已发货的订单");
        }
        
        orderMapper.updateStatus(orderId, "completed");
    }
    
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        
        // 获取订单项
        List<OrderItem> orderItems = orderMapper.selectOrderItemsByOrderId(order.getId());
        List<OrderVO.OrderItemVO> itemVOs = orderItems.stream().map(item -> {
            OrderVO.OrderItemVO itemVO = new OrderVO.OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            
            // 获取商品信息
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                // TODO: 创建ProductVO
            }
            
            return itemVO;
        }).collect(Collectors.toList());
        
        vo.setItems(itemVOs);
        
        // TODO: 获取买家和卖家信息
        // vo.setBuyerInfo(userService.getUserInfo(order.getBuyerId()));
        // vo.setSellerInfo(userService.getUserInfo(order.getSellerId()));
        
        return vo;
    }
}