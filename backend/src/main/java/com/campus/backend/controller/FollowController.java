package com.campus.backend.controller;

import com.campus.backend.common.Result;
import com.campus.backend.common.SecurityUtils;
import com.campus.backend.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v2/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggleFollow(@RequestParam Long followeeId) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean isFollowing = followService.toggleFollow(userId, followeeId);
        return Result.success(Map.of("isFollowing", isFollowing));
    }

    @GetMapping("/check")
    public Result<Map<String, Object>> checkFollowing(@RequestParam Long followeeId) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        boolean isFollowing = userId != null && followService.isFollowing(userId, followeeId);
        return Result.success(Map.of("isFollowing", isFollowing));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getFollowStats(@RequestParam Long userId) {
        int followingCount = followService.getFollowingCount(userId);
        int followerCount = followService.getFollowerCount(userId);
        return Result.success(Map.of("followingCount", followingCount, "followerCount", followerCount));
    }
}
