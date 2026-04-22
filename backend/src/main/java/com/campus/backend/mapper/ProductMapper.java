package com.campus.backend.mapper;

import com.campus.backend.entity.Product;
import com.campus.backend.dto.ProductQueryDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProductMapper {
    List<Product> selectProductList(@Param("query") ProductQueryDTO query);
    int selectProductCount(@Param("query") ProductQueryDTO query);
    
    @Select("SELECT * FROM products WHERE id = #{id}")
    Product selectById(Long id);
    
    @Insert("INSERT INTO products (name, description, price, category_id, seller_id, status, image_url) " +
            "VALUES (#{name}, #{description}, #{price}, #{categoryId}, #{sellerId}, #{status}, #{imageUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);
    
    @Update("UPDATE products SET name = #{name}, description = #{description}, price = #{price}, " +
            "category_id = #{categoryId}, status = #{status}, image_url = #{imageUrl}, updated_at = CURRENT_TIMESTAMP " +
            "WHERE id = #{id}")
    int update(Product product);
    
    @Update("UPDATE products SET status = #{status}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    @Select("SELECT * FROM products WHERE seller_id = #{sellerId} ORDER BY created_at DESC")
    List<Product> selectBySellerId(Long sellerId);
}