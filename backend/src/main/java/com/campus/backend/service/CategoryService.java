package com.campus.backend.service;

import com.campus.backend.common.Result;
import com.campus.backend.dto.CategoryCreateDTO;
import com.campus.backend.dto.CategoryUpdateDTO;
import com.campus.backend.dto.CategoryVO;

import java.util.List;

public interface CategoryService {
    
    Result<CategoryVO> createCategory(CategoryCreateDTO createDTO);
    
    Result<CategoryVO> updateCategory(Long id, CategoryUpdateDTO updateDTO);
    
    Result<Void> deleteCategory(Long id);
    
    Result<CategoryVO> getCategoryById(Long id);
    
    Result<List<CategoryVO>> getAllCategories();
    
    Result<List<CategoryVO>> getRootCategories();
    
    Result<List<CategoryVO>> getCategoriesByParentId(Long parentId);
    
    Result<List<CategoryVO>> getCategoriesByStatus(Integer status);
    
    Result<List<CategoryVO>> getCategoryTree();
    
    Result<Void> updateCategoryStatus(Long id, Integer status);
}