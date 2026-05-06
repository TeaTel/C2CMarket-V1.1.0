package com.campus.backend.controller;

import com.campus.backend.common.Result;
import com.campus.backend.common.SecurityUtils;
import com.campus.backend.dto.CommentCreateDTO;
import com.campus.backend.dto.PostCommentVO;
import com.campus.backend.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v2/posts")
@RequiredArgsConstructor
public class PostCommentController {

    private final PostService postService;

    @PostMapping("/{postId}/comments")
    public Result<PostCommentVO> addComment(@PathVariable Long postId, @Valid @RequestBody CommentCreateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        PostCommentVO comment = postService.addComment(postId, dto, userId);
        return Result.success(comment);
    }

    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        postService.deleteComment(commentId, userId);
        return Result.success(null);
    }

    @GetMapping("/{postId}/comments")
    public Result<List<PostCommentVO>> getComments(@PathVariable Long postId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserIdOrNull();
        List<PostCommentVO> comments = postService.getComments(postId, userId, page, size);
        return Result.success(comments);
    }
}
