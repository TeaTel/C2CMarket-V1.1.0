package com.campus.backend.service;

import com.campus.backend.dto.ProductCreateDTO;
import com.campus.backend.dto.ProductQueryDTO;
import com.campus.backend.dto.ProductUpdateDTO;
import com.campus.backend.dto.ProductVO;

import java.util.List;

public interface ProductService {
    List<ProductVO> getProductList(ProductQueryDTO query);
    int getProductCount(ProductQueryDTO query);
    
    ProductVO getProductDetail(Long productId);
    
    ProductVO createProduct(ProductCreateDTO createDTO, Long sellerId);
    
    ProductVO updateProduct(Long productId, ProductUpdateDTO updateDTO, Long sellerId);
    
    void deleteProduct(Long productId, Long sellerId);
    
    List<ProductVO> getMyProducts(Long sellerId);
}