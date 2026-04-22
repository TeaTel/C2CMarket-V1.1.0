package com.campus.backend.mapper;

import com.campus.backend.entity.Order;
import com.campus.backend.entity.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OrderMapper {
    
    @Insert("INSERT INTO orders (buyer_id, seller_id, total_amount, status, payment_method, address) " +
            "VALUES (#{buyerId}, #{sellerId}, #{totalAmount}, 'pending', #{paymentMethod}, #{address})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);
    
    @Select("SELECT * FROM orders WHERE id = #{id}")
    Order selectById(Long id);
    
    @Select("SELECT * FROM orders WHERE buyer_id = #{buyerId} ORDER BY created_at DESC")
    List<Order> selectByBuyerId(Long buyerId);
    
    @Select("SELECT * FROM orders WHERE seller_id = #{sellerId} ORDER BY created_at DESC")
    List<Order> selectBySellerId(Long sellerId);
    
    @Update("UPDATE orders SET status = #{status}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    @Update("UPDATE orders SET status = #{status}, payment_method = #{paymentMethod}, " +
            "address = #{address}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(Order order);
    
    // 订单项相关操作
    @Insert("INSERT INTO order_items (order_id, product_id, quantity, price) " +
            "VALUES (#{orderId}, #{productId}, #{quantity}, #{price})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrderItem(OrderItem orderItem);
    
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId}")
    List<OrderItem> selectOrderItemsByOrderId(Long orderId);
    
    @Select("SELECT * FROM order_items WHERE product_id = #{productId}")
    List<OrderItem> selectOrderItemsByProductId(Long productId);
}