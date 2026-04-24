package com.campus.backend.mapper;

import com.campus.backend.entity.Order;
import com.campus.backend.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单数据访问层
 */
@Mapper
public interface OrderMapper {

    /**
     * 新增订单
     */
    @Insert("INSERT INTO orders (order_no, buyer_id, seller_id, total_amount, status, remark, address) " +
            "VALUES (#{orderNo}, #{buyerId}, #{sellerId}, #{totalAmount}, 'pending', #{remark}, #{address})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    /**
     * 根据ID查询订单
     */
    @Select("SELECT * FROM orders WHERE id = #{id}")
    Order selectById(Long id);

    /**
     * 根据订单编号查询
     */
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询买家的订单列表
     */
    @Select("SELECT * FROM orders WHERE buyer_id = #{buyerId} ORDER BY created_at DESC")
    List<Order> selectByBuyerId(@Param("buyerId") Long buyerId);

    /**
     * 查询卖家的订单列表
     */
    @Select("SELECT * FROM orders WHERE seller_id = #{sellerId} ORDER BY created_at DESC")
    List<Order> selectBySellerId(@Param("sellerId") Long sellerId);

    /**
     * 更新订单状态
     */
    @Update("<script>" +
            "UPDATE orders SET status = #{status}, updated_at = CURRENT_TIMESTAMP " +
            "<if test='completedAt != null'>, completed_at = #{completedAt}</if> " +
            "WHERE id = #{id}" +
            "</script>")
    int updateStatus(
        @Param("id") Long id,
        @Param("status") String status,
        @Param("completedAt") java.time.LocalDateTime completedAt
    );

    /**
     * 新增订单项
     */
    @Insert("INSERT INTO order_items (order_id, product_id, product_name, product_image, price, quantity, subtotal) " +
            "VALUES (#{orderId}, #{productId}, #{productName}, #{productImage}, #{price}, #{quantity}, #{subtotal})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrderItem(OrderItem orderItem);

    /**
     * 查询订单的所有订单项
     */
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId}")
    List<OrderItem> selectOrderItemsByOrderId(@Param("orderId") Long orderId);

    /**
     * 统计买家订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE buyer_id = #{buyerId}")
    int countByBuyerId(@Param("buyerId") Long buyerId);
}
