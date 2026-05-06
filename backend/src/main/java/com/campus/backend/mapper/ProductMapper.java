package com.campus.backend.mapper;

import com.campus.backend.entity.Product;
import com.campus.backend.dto.ProductQueryDTO;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 商品数据访问层
 * 复杂查询使用XML映射文件 (ProductMapper.xml)
 */
@Mapper
public interface ProductMapper {

    /**
     * 查询商品列表 (动态条件，见XML)
     */
    List<Product> selectProductList(@Param("query") ProductQueryDTO query);

    /**
     * 查询商品总数
     */
    int selectProductCount(@Param("query") ProductQueryDTO query);

    /**
     * 根据ID查询商品
     */
    @Select("SELECT * FROM products WHERE id = #{id}")
    Product selectById(Long id);

    /**
     * 新增商品
     */
    @Insert("INSERT INTO products (name, description, price, original_price, category_id, seller_id, " +
            "condition_level, image_urls, cover_image, location, delivery_method, status, " +
            "story_title, story_content, story_images, has_story, sale_mode) " +
            "VALUES (#{name}, #{description}, #{price}, #{originalPrice}, #{categoryId}, #{sellerId}, " +
            "#{conditionLevel}, #{imageUrls}, #{coverImage}, #{location}, #{deliveryMethod}, 1, " +
            "#{storyTitle}, #{storyContent}, #{storyImages}, #{hasStory}, #{saleMode})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    /**
     * 更新商品
     */
    int update(Product product);

    /**
     * 更新商品状态 (上架/下架/售出)
     */
    @Update("UPDATE products SET status = #{status}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 增加浏览次数
     */
    @Update("UPDATE products SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 增加收藏数
     */
    @Update("UPDATE products SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(@Param("id") Long id);

    /**
     * 减少收藏数
     */
    @Update("UPDATE products SET like_count = GREATEST(0, like_count - 1) WHERE id = #{id}")
    int decrementLikeCount(@Param("id") Long id);

    /**
     * 查询某卖家的所有商品
     */
    @Select("SELECT * FROM products WHERE seller_id = #{sellerId} ORDER BY created_at DESC")
    List<Product> selectBySellerId(@Param("sellerId") Long sellerId);

    /**
     * 查询某分类下的商品数量
     */
    @Select("SELECT COUNT(*) FROM products WHERE category_id = #{categoryId}")
    int countByCategoryId(@Param("categoryId") Long categoryId);
}
