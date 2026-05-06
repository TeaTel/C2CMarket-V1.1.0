package com.campus.backend.controller;

import com.campus.backend.common.Result;
import com.campus.backend.common.SecurityUtils;
import com.campus.backend.dto.*;
import com.campus.backend.service.PostService;
import com.campus.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v2/feed")
@RequiredArgsConstructor
public class FeedController {

    private final PostService postService;
    private final ProductService productService;

    @GetMapping
    public Result<Map<String, Object>> getFeed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String feedType) {

        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        List<FeedItemVO> items = new ArrayList<>();

        PostQueryDTO postQuery = new PostQueryDTO();
        postQuery.setPage(page);
        postQuery.setSize(size / 2);
        List<PostVO> posts = postService.getPostList(postQuery, userId);

        for (PostVO post : posts) {
            FeedItemVO item = new FeedItemVO();
            item.setItemType("POST");
            item.setId(post.getId());
            item.setTitle(post.getTitle());
            item.setContent(post.getContent());
            item.setLikeCount(post.getLikeCount());
            item.setCommentCount(post.getCommentCount());
            item.setViewCount(post.getViewCount());
            item.setIsLiked(post.getIsLiked());
            item.setUserId(post.getUserId());
            item.setUserName(post.getUserName());
            item.setUserAvatar(post.getUserAvatar());
            item.setBoardId(post.getBoardId());
            item.setBoardName(post.getBoardName());
            item.setPostType(post.getPostType());
            item.setCreatedAt(post.getCreatedAt());
            items.add(item);
        }

        ProductQueryDTO productQuery = new ProductQueryDTO();
        productQuery.setPage(page);
        productQuery.setSize(size / 2);
        productQuery.setStatus(1);
        List<ProductVO> products = productService.getProductList(productQuery);

        for (ProductVO product : products) {
            FeedItemVO item = new FeedItemVO();
            item.setItemType("PRODUCT");
            item.setId(product.getId());
            item.setTitle(product.getName());
            item.setContent(product.getDescription());
            item.setCoverImage(product.getCoverImage());
            item.setPrice(product.getPrice() != null ? product.getPrice().toString() : null);
            item.setOriginalPrice(product.getOriginalPrice() != null ? product.getOriginalPrice().toString() : null);
            item.setLikeCount(product.getLikeCount());
            item.setViewCount(product.getViewCount());
            item.setIsLiked(product.getIsFavorited());
            item.setUserId(product.getSellerId());
            item.setUserName(product.getSellerName());
            item.setUserAvatar(product.getSellerAvatar());
            item.setCategoryId(product.getCategoryId());
            item.setCategoryName(product.getCategoryName());
            item.setHasStory(product.getHasStory());
            item.setConditionText(product.getConditionText());
            item.setLocation(product.getLocation());
            item.setCreatedAt(product.getCreatedAt());
            items.add(item);
        }

        Collections.shuffle(items);

        Map<String, Object> result = Map.of(
                "list", items,
                "page", page,
                "size", size
        );
        return Result.success(result);
    }
}
