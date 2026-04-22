package com.campus.backend.service.impl;

import com.campus.backend.dto.CategoryVO;
import com.campus.backend.dto.ProductCreateDTO;
import com.campus.backend.dto.ProductQueryDTO;
import com.campus.backend.dto.ProductUpdateDTO;
import com.campus.backend.dto.ProductVO;
import com.campus.backend.dto.UserVO;
import com.campus.backend.entity.Product;
import com.campus.backend.mapper.ProductMapper;
import com.campus.backend.service.CategoryService;
import com.campus.backend.service.ProductService;
import com.campus.backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Override
    public List<ProductVO> getProductList(ProductQueryDTO query) {
        List<Product> products = productMapper.selectProductList(query);
        return products.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public int getProductCount(ProductQueryDTO query) {
        return productMapper.selectProductCount(query);
    }

    @Override
    public ProductVO getProductDetail(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return convertToVO(product);
    }

    @Override
    @Transactional
    public ProductVO createProduct(ProductCreateDTO createDTO, Long sellerId) {
        Product product = new Product();
        BeanUtils.copyProperties(createDTO, product);
        product.setSellerId(sellerId);
        product.setStatus(1);  // active状态
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        productMapper.insert(product);
        return convertToVO(product);
    }

    @Override
    @Transactional
    public ProductVO updateProduct(Long productId, ProductUpdateDTO updateDTO, Long sellerId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 检查权限：只有卖家本人可以修改商品
        if (!product.getSellerId().equals(sellerId)) {
            throw new RuntimeException("无权修改此商品");
        }
        
        BeanUtils.copyProperties(updateDTO, product);
        product.setUpdatedAt(LocalDateTime.now());
        
        productMapper.update(product);
        return convertToVO(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 检查权限：只有卖家本人可以删除商品
        if (!product.getSellerId().equals(sellerId)) {
            throw new RuntimeException("无权删除此商品");
        }
        
        // 软删除：将状态改为0 (removed)
        productMapper.updateStatus(productId, 0);
    }

    @Override
    public List<ProductVO> getMyProducts(Long sellerId) {
        List<Product> products = productMapper.selectBySellerId(sellerId);
        return products.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        // 手动复制字段
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setCategoryId(product.getCategoryId());
        vo.setSellerId(product.getSellerId());
        vo.setStatus(product.getStatus());
        vo.setImageUrl(product.getImageUrl());

        // 查询分类名称
        try {
            if (product.getCategoryId() != null) {
                var categoryResult = categoryService.getCategoryById(product.getCategoryId());
                if (categoryResult != null && categoryResult.getData() != null) {
                    vo.setCategoryName(categoryResult.getData().getName());
                }
            }
        } catch (Exception e) {
            System.err.println("获取分类名称失败: " + e.getMessage());
        }

        // 查询卖家名称
        try {
            if (product.getSellerId() != null) {
                UserVO user = userService.getUserInfo(product.getSellerId());
                if (user != null) {
                    vo.setSellerName(user.getUsername());
                }
            }
        } catch (Exception e) {
            System.err.println("获取卖家名称失败: " + e.getMessage());
        }

        return vo;
    }
}