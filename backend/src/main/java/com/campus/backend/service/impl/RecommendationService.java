package com.campus.backend.service.impl;

import com.campus.backend.dto.FeedItemVO;
import com.campus.backend.dto.PostVO;
import com.campus.backend.dto.PostQueryDTO;
import com.campus.backend.dto.ProductVO;
import com.campus.backend.dto.ProductQueryDTO;
import com.campus.backend.entity.User;
import com.campus.backend.mapper.PostMapper;
import com.campus.backend.mapper.ProductMapper;
import com.campus.backend.mapper.UserBehaviorMapper;
import com.campus.backend.mapper.UserLikeMapper;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.service.PostService;
import com.campus.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserBehaviorMapper userBehaviorMapper;
    private final PostMapper postMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final UserLikeMapper userLikeMapper;
    private final PostService postService;
    private final ProductService productService;

    public List<FeedItemVO> recommend(Long userId, int limit) {
        Set<String> interestTags = new LinkedHashSet<>();

        List<Long> recentPostIds = userBehaviorMapper.selectRecentTargetIds(userId, "POST", 10);
        for (Long postId : recentPostIds) {
            var post = postMapper.selectById(postId);
            if (post != null && post.getTags() != null) {
                for (String tag : post.getTags().split(",")) {
                    String t = tag.trim();
                    if (!t.isEmpty()) interestTags.add(t);
                }
            }
        }

        List<Long> recentProductIds = userBehaviorMapper.selectRecentTargetIds(userId, "PRODUCT", 10);
        for (Long productId : recentProductIds) {
            var product = productMapper.selectById(productId);
            if (product != null && product.getTags() != null) {
                for (String tag : product.getTags().split(",")) {
                    String t = tag.trim();
                    if (!t.isEmpty()) interestTags.add(t);
                }
            }
        }

        if (interestTags.isEmpty()) {
            return Collections.emptyList();
        }

        List<ScoredItem> scoredItems = new ArrayList<>();

        PostQueryDTO postQuery = new PostQueryDTO();
        postQuery.setPage(1);
        postQuery.setSize(50);
        List<PostVO> allPosts = postService.getPostList(postQuery, userId);
        for (PostVO post : allPosts) {
            if (recentPostIds.contains(post.getId())) continue;
            int score = matchScore(post.getTags(), interestTags);
            if (score > 0) {
                var item = new ScoredItem();
                item.itemType = "POST";
                item.id = post.getId();
                item.score = score;
                scoredItems.add(item);
            }
        }

        ProductQueryDTO productQuery = new ProductQueryDTO();
        productQuery.setPage(1);
        productQuery.setSize(50);
        productQuery.setStatus(1);
        List<ProductVO> allProducts = productService.getProductList(productQuery);
        for (ProductVO product : allProducts) {
            if (recentProductIds.contains(product.getId())) continue;
            int score = matchScore(product.getTags(), interestTags);
            if (score > 0) {
                var item = new ScoredItem();
                item.itemType = "PRODUCT";
                item.id = product.getId();
                item.score = score;
                scoredItems.add(item);
            }
        }

        scoredItems.sort((a, b) -> Integer.compare(b.score, a.score));

        List<FeedItemVO> result = new ArrayList<>();
        for (ScoredItem si : scoredItems) {
            if (result.size() >= limit) break;
            FeedItemVO vo = null;
            if ("POST".equals(si.itemType)) {
                var postEntity = postMapper.selectById(si.id);
                if (postEntity == null) continue;
                vo = new FeedItemVO();
                vo.setItemType("POST");
                vo.setId(postEntity.getId());
                vo.setTitle(postEntity.getTitle());
                vo.setContent(postEntity.getContent());
                vo.setLikeCount(postEntity.getLikeCount());
                vo.setCommentCount(postEntity.getCommentCount());
                vo.setViewCount(postEntity.getViewCount());
                vo.setUserId(postEntity.getUserId());
                vo.setPostType(postEntity.getPostType());
                vo.setCreatedAt(postEntity.getCreatedAt());
                vo.setLocation(postEntity.getLocation());
                User u = userMapper.selectById(postEntity.getUserId());
                if (u != null) {
                    vo.setUserName(u.getNickname() != null ? u.getNickname() : u.getUsername());
                    vo.setUserAvatar(u.getAvatar());
                }
                vo.setIsLiked(userLikeMapper.exists(userId, "POST", postEntity.getId()));
            } else {
                var productEntity = productMapper.selectById(si.id);
                if (productEntity == null) continue;
                vo = new FeedItemVO();
                vo.setItemType("PRODUCT");
                vo.setId(productEntity.getId());
                vo.setTitle(productEntity.getName());
                vo.setContent(productEntity.getDescription());
                vo.setCoverImage(productEntity.getCoverImage());
                vo.setPrice(productEntity.getPrice() != null ? productEntity.getPrice().toString() : null);
                vo.setOriginalPrice(productEntity.getOriginalPrice() != null ? productEntity.getOriginalPrice().toString() : null);
                vo.setLikeCount(productEntity.getLikeCount());
                vo.setViewCount(productEntity.getViewCount());
                vo.setUserId(productEntity.getSellerId());
                vo.setLocation(productEntity.getLocation());
                vo.setCreatedAt(productEntity.getCreatedAt());
                User u = userMapper.selectById(productEntity.getSellerId());
                if (u != null) {
                    vo.setUserName(u.getNickname() != null ? u.getNickname() : u.getUsername());
                    vo.setUserAvatar(u.getAvatar());
                }
                vo.setIsLiked(userLikeMapper.exists(userId, "PRODUCT", productEntity.getId()));
            }
            if (vo != null) result.add(vo);
        }

        log.info("推荐: userId={}, interestTags={}, results={}", userId, interestTags, result.size());
        return result;
    }

    private int matchScore(String itemTags, Set<String> interestTags) {
        if (itemTags == null || itemTags.isEmpty()) return 0;
        int score = 0;
        for (String tag : itemTags.split(",")) {
            if (interestTags.contains(tag.trim())) score++;
        }
        return score;
    }

    private static class ScoredItem {
        String itemType;
        Long id;
        int score;
    }
}
