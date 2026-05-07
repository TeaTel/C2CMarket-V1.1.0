package com.campus.backend.controller;

import com.campus.backend.common.Result;
import com.campus.backend.common.SecurityUtils;
import com.campus.backend.dto.*;
import com.campus.backend.entity.UserFollow;
import com.campus.backend.entity.User;
import com.campus.backend.mapper.UserFollowMapper;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.service.PostService;
import com.campus.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v2/feed")
@RequiredArgsConstructor
public class FeedController {

    private final PostService postService;
    private final ProductService productService;
    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;

    @GetMapping
    public Result<Map<String, Object>> getFeed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long circleId) {

        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        List<FeedItemVO> items = new ArrayList<>();

        PostQueryDTO postQuery = new PostQueryDTO();
        postQuery.setPage(page);
        postQuery.setSize(size / 2);

        ProductQueryDTO productQuery = new ProductQueryDTO();
        productQuery.setPage(page);
        productQuery.setSize(size / 2);
        productQuery.setStatus(1);

        if (circleId != null) {
            postQuery.setBoardId(circleId);
            productQuery.setCategoryId(circleId.intValue());
        }

        if ("following".equals(type) && userId != null) {
            List<Long> followeeIds = getFolloweeIds(userId);
            if (followeeIds.isEmpty()) {
                return Result.success(Map.of("list", List.of(), "page", page, "size", size, "total", 0));
            }
            postQuery.setUserIds(followeeIds);
            productQuery.setSellerIds(followeeIds);
        } else if ("campus".equals(type) && userId != null) {
            List<Long> campusUserIds = getCampusUserIds(userId);
            if (campusUserIds.isEmpty()) {
                return Result.success(Map.of("list", List.of(), "page", page, "size", size, "total", 0));
            }
            postQuery.setUserIds(campusUserIds);
            productQuery.setSellerIds(campusUserIds);
        }

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
                "size", size,
                "total", items.size()
        );
        return Result.success(result);
    }

    private List<Long> getFolloweeIds(Long userId) {
        List<UserFollow> follows = userFollowMapper.selectFollowing(userId, 0, 200);
        return follows.stream()
                .map(UserFollow::getFolloweeId)
                .collect(Collectors.toList());
    }

    private List<Long> getCampusUserIds(Long userId) {
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null || currentUser.getSchool() == null || currentUser.getSchool().isEmpty()) {
            return List.of();
        }
        return userMapper.selectIdsBySchool(currentUser.getSchool());
    }
}
