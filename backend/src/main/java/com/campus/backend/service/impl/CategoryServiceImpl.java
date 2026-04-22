package com.campus.backend.service.impl;

import com.campus.backend.common.Result;
import com.campus.backend.dto.CategoryCreateDTO;
import com.campus.backend.dto.CategoryUpdateDTO;
import com.campus.backend.dto.CategoryVO;
import com.campus.backend.entity.Category;
import com.campus.backend.mapper.CategoryMapper;
import com.campus.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public Result<CategoryVO> createCategory(CategoryCreateDTO createDTO) {
        // 检查分类名称是否已存在
        Category existingCategory = categoryMapper.findByName(createDTO.getName());
        if (existingCategory != null) {
            return Result.error("分类名称已存在");
        }

        // 检查父分类是否存在
        if (createDTO.getParentId() != null) {
            Category parentCategory = categoryMapper.findById(createDTO.getParentId());
            if (parentCategory == null) {
                return Result.error("父分类不存在");
            }
        }

        // 创建分类
        Category category = new Category();
        category.setName(createDTO.getName());
        category.setDescription(createDTO.getDescription());
        category.setSortOrder(createDTO.getSortOrder() != null ? createDTO.getSortOrder() : 0);
        category.setStatus(1); // 默认启用
        category.setParentId(createDTO.getParentId());
        category.setIconUrl(createDTO.getIconUrl());
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        int result = categoryMapper.insert(category);
        if (result > 0) {
            return Result.success("分类创建成功", convertToVO(category));
        }
        return Result.error("分类创建失败");
    }

    @Override
    @Transactional
    public Result<CategoryVO> updateCategory(Long id, CategoryUpdateDTO updateDTO) {
        // 检查分类是否存在
        Category category = categoryMapper.findById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        // 检查分类名称是否已存在（排除自身）
        if (updateDTO.getName() != null && !updateDTO.getName().equals(category.getName())) {
            Category existingCategory = categoryMapper.findByName(updateDTO.getName());
            if (existingCategory != null && !existingCategory.getId().equals(id)) {
                return Result.error("分类名称已存在");
            }
            category.setName(updateDTO.getName());
        }

        // 检查父分类是否存在（不能设置自己为父分类）
        if (updateDTO.getParentId() != null) {
            if (updateDTO.getParentId().equals(id)) {
                return Result.error("不能设置自己为父分类");
            }
            Category parentCategory = categoryMapper.findById(updateDTO.getParentId());
            if (parentCategory == null) {
                return Result.error("父分类不存在");
            }
            category.setParentId(updateDTO.getParentId());
        }

        // 更新其他字段
        if (updateDTO.getDescription() != null) {
            category.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getSortOrder() != null) {
            category.setSortOrder(updateDTO.getSortOrder());
        }
        if (updateDTO.getStatus() != null) {
            category.setStatus(updateDTO.getStatus());
        }
        if (updateDTO.getIconUrl() != null) {
            category.setIconUrl(updateDTO.getIconUrl());
        }
        category.setUpdatedAt(LocalDateTime.now());

        int result = categoryMapper.update(category);
        if (result > 0) {
            return Result.success("分类更新成功", convertToVO(category));
        }
        return Result.error("分类更新失败");
    }

    @Override
    @Transactional
    public Result<Void> deleteCategory(Long id) {
        // 检查分类是否存在
        Category category = categoryMapper.findById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        // 检查是否有子分类
        int childCount = categoryMapper.countByParentId(id);
        if (childCount > 0) {
            return Result.error("该分类下有子分类，无法删除");
        }

        // 检查是否有商品使用该分类
        int productCount = categoryMapper.countProductsByCategoryId(id);
        if (productCount > 0) {
            return Result.error("该分类下有商品，无法删除");
        }

        int result = categoryMapper.deleteById(id);
        if (result > 0) {
            return Result.success();
        }
        return Result.error();
    }

    @Override
    public Result<CategoryVO> getCategoryById(Long id) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        return Result.success(convertToVO(category));
    }

    @Override
    public Result<List<CategoryVO>> getAllCategories() {
        List<Category> categories = categoryMapper.findAll();
        List<CategoryVO> categoryVOs = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(categoryVOs);
    }

    @Override
    public Result<List<CategoryVO>> getRootCategories() {
        List<Category> categories = categoryMapper.findRootCategories();
        List<CategoryVO> categoryVOs = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(categoryVOs);
    }

    @Override
    public Result<List<CategoryVO>> getCategoriesByParentId(Long parentId) {
        List<Category> categories = categoryMapper.findByParentId(parentId);
        List<CategoryVO> categoryVOs = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(categoryVOs);
    }

    @Override
    public Result<List<CategoryVO>> getCategoriesByStatus(Integer status) {
        List<Category> categories = categoryMapper.findByStatus(status);
        List<CategoryVO> categoryVOs = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(categoryVOs);
    }

    @Override
    public Result<List<CategoryVO>> getCategoryTree() {
        List<Category> allCategories = categoryMapper.findAll();
        List<CategoryVO> rootCategories = allCategories.stream()
                .filter(category -> category.getParentId() == null)
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建树形结构
        for (CategoryVO rootCategory : rootCategories) {
            buildCategoryTree(rootCategory, allCategories);
        }

        return Result.success(rootCategories);
    }

    @Override
    @Transactional
    public Result<Void> updateCategoryStatus(Long id, Integer status) {
        // 检查分类是否存在
        Category category = categoryMapper.findById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        // 验证状态值
        if (status != 0 && status != 1) {
            return Result.error("状态值无效");
        }

        category.setStatus(status);
        category.setUpdatedAt(LocalDateTime.now());

        int result = categoryMapper.update(category);
        if (result > 0) {
            return Result.success();
        }
        return Result.error();
    }

    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setDescription(category.getDescription());
        vo.setSortOrder(category.getSortOrder());
        vo.setStatus(category.getStatus());
        vo.setParentId(category.getParentId());
        vo.setIconUrl(category.getIconUrl());
        vo.setCreatedAt(category.getCreatedAt());
        vo.setUpdatedAt(category.getUpdatedAt());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    private void buildCategoryTree(CategoryVO parentCategory, List<Category> allCategories) {
        List<CategoryVO> children = allCategories.stream()
                .filter(category -> category.getParentId() != null && category.getParentId().equals(parentCategory.getId()))
                .map(this::convertToVO)
                .collect(Collectors.toList());

        parentCategory.setChildren(children);

        for (CategoryVO child : children) {
            buildCategoryTree(child, allCategories);
        }
    }
}