package com.campus.backend.controller;

import com.campus.backend.common.Result;
import com.campus.backend.common.SecurityUtils;
import com.campus.backend.dto.ProductVO;
import com.campus.backend.entity.Favorite;
import com.campus.backend.entity.Product;
import com.campus.backend.mapper.FavoriteMapper;
import com.campus.backend.mapper.ProductMapper;
import com.campus.backend.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "收藏管理", description = "商品收藏功能")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    @PostMapping("/{productId}")
    @Operation(summary = "收藏商品")
    public Result<Void> addFavorite(@PathVariable Long productId) {
        favoriteService.addFavorite(SecurityUtils.getCurrentUserId(), productId);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "取消收藏")
    public Result<Void> removeFavorite(@PathVariable Long productId) {
        favoriteService.removeFavorite(SecurityUtils.getCurrentUserId(), productId);
        return Result.success("已取消收藏");
    }

    @GetMapping("/{productId}/check")
    @Operation(summary = "检查是否已收藏")
    public Result<Boolean> checkFavorited(@PathVariable Long productId) {
        return Result.success(favoriteService.isFavorited(SecurityUtils.getCurrentUserId(), productId));
    }

    @GetMapping
    @Operation(summary = "我的收藏列表")
    public Result<Map<String, Object>> getMyFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        int offset = (page - 1) * size;

        List<Favorite> favorites = favoriteMapper.selectByUserId(userId, offset, size);
        int total = favoriteMapper.countByUserId(userId);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Favorite fav : favorites) {
            try {
                Product product = productMapper.selectById(fav.getProductId());
                if (product != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", product.getId());
                    item.put("name", product.getName());
                    item.put("price", product.getPrice());
                    item.put("originalPrice", product.getOriginalPrice());
                    item.put("coverImage", product.getCoverImage());
                    item.put("conditionLevel", product.getConditionLevel());
                    item.put("status", product.getStatus());
                    item.put("categoryId", product.getCategoryId());
                    item.put("location", product.getLocation());
                    item.put("createdAt", product.getCreatedAt());
                    item.put("favoritedAt", fav.getCreatedAt());
                    items.add(item);
                }
            } catch (Exception ignored) {}
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", items);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    @GetMapping("/count")
    @Operation(summary = "我的收藏数量")
    public Result<Integer> getMyFavoriteCount() {
        return Result.success(favoriteMapper.countByUserId(SecurityUtils.getCurrentUserId()));
    }
}
