package com.campus.backend.mapper;

import com.campus.backend.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {
    
    @Insert("INSERT INTO categories (name, description, sort_order, status, parent_id, icon_url, created_at, updated_at) " +
            "VALUES (#{name}, #{description}, #{sortOrder}, #{status}, #{parentId}, #{iconUrl}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);
    
    @Update("UPDATE categories SET " +
            "name = #{name}, " +
            "description = #{description}, " +
            "sort_order = #{sortOrder}, " +
            "status = #{status}, " +
            "parent_id = #{parentId}, " +
            "icon_url = #{iconUrl}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    int update(Category category);
    
    @Delete("DELETE FROM categories WHERE id = #{id}")
    int deleteById(Long id);
    
    @Select("SELECT * FROM categories WHERE id = #{id}")
    Category findById(Long id);
    
    @Select("SELECT * FROM categories WHERE name = #{name}")
    Category findByName(String name);
    
    @Select("SELECT * FROM categories WHERE parent_id = #{parentId} ORDER BY sort_order ASC, created_at DESC")
    List<Category> findByParentId(Long parentId);
    
    @Select("SELECT * FROM categories WHERE status = #{status} ORDER BY sort_order ASC, created_at DESC")
    List<Category> findByStatus(Integer status);
    
    @Select("SELECT * FROM categories ORDER BY sort_order ASC, created_at DESC")
    List<Category> findAll();
    
    @Select("SELECT * FROM categories WHERE parent_id IS NULL ORDER BY sort_order ASC, created_at DESC")
    List<Category> findRootCategories();
    
    @Select("SELECT COUNT(*) FROM categories WHERE parent_id = #{parentId}")
    int countByParentId(Long parentId);
    
    @Select("SELECT COUNT(*) FROM products WHERE category_id = #{categoryId}")
    int countProductsByCategoryId(Long categoryId);
}