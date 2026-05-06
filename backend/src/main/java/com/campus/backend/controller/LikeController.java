package com.campus.backend.controller;

import com.campus.backend.common.Result;
import com.campus.backend.common.SecurityUtils;
import com.campus.backend.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v2/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggleLike(@RequestParam String targetType, @RequestParam Long targetId) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean isLiked = likeService.toggleLike(userId, targetType, targetId);
        int likeCount = likeService.getLikeCount(targetType, targetId);
        return Result.success(Map.of("isLiked", isLiked, "likeCount", likeCount));
    }

    @GetMapping("/check")
    public Result<Map<String, Object>> checkLiked(@RequestParam String targetType, @RequestParam Long targetId) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        boolean isLiked = userId != null && likeService.isLiked(userId, targetType, targetId);
        int likeCount = likeService.getLikeCount(targetType, targetId);
        return Result.success(Map.of("isLiked", isLiked, "likeCount", likeCount));
    }

    @GetMapping("/count")
    public Result<Map<String, Object>> getLikeCount(@RequestParam String targetType, @RequestParam Long targetId) {
        int likeCount = likeService.getLikeCount(targetType, targetId);
        return Result.success(Map.of("likeCount", likeCount));
    }
}
