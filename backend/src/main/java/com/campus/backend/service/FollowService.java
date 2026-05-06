package com.campus.backend.service;

public interface FollowService {

    boolean toggleFollow(Long followerId, Long followeeId);

    boolean isFollowing(Long followerId, Long followeeId);

    int getFollowingCount(Long userId);

    int getFollowerCount(Long userId);
}
