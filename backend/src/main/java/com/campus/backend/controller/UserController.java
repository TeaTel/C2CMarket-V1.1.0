package com.campus.backend.controller;

import com.campus.backend.common.Result;
import com.campus.backend.common.SecurityUtils;
import com.campus.backend.config.JwtUtil;
import com.campus.backend.dto.*;
import com.campus.backend.entity.User;
import com.campus.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "注册、登录、个人信息")
public class UserController {

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private JwtUtil jwtUtil;

    private UserService svc() {
        if (userService == null) {
            throw new IllegalStateException("UserService not initialized");
        }
        return userService;
    }

    private JwtUtil jwt() {
        if (jwtUtil == null) {
            throw new IllegalStateException("JwtUtil not initialized");
        }
        return jwtUtil;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Map<String, Object>> register(@Valid @RequestBody UserRegisterDTO dto) {
        UserVO userVO = svc().register(dto);
        String token = jwt().generateToken(userVO.getId(), userVO.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("user", userVO);
        result.put("token", token);
        return Result.success("注册成功", result);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<Map<String, Object>> login(@Valid @RequestBody UserLoginDTO dto) {
        UserVO userVO = svc().login(dto.getUsername(), dto.getPassword());
        String token = jwt().generateToken(userVO.getId(), userVO.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("user", userVO);
        result.put("token", token);
        return Result.success("登录成功", result);
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<UserVO> getUserInfo() {
        return Result.success(svc().getUserInfo(SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人资料")
    public Result<UserVO> updateProfile(@RequestBody User profileData) {
        Long userId = SecurityUtils.getCurrentUserId();
        User safeData = new User();
        safeData.setNickname(profileData.getNickname());
        safeData.setAvatar(profileData.getAvatar());
        safeData.setGender(profileData.getGender());
        safeData.setSchool(profileData.getSchool());
        safeData.setMajor(profileData.getMajor());
        safeData.setGrade(profileData.getGrade());
        safeData.setWechat(profileData.getWechat());
        safeData.setQq(profileData.getQq());
        safeData.setBio(profileData.getBio());
        return Result.success(svc().updateProfile(userId, safeData));
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> updatePassword(@RequestBody Map<String, String> body) {
        svc().updatePassword(
                SecurityUtils.getCurrentUserId(),
                body.get("oldPassword"),
                body.get("newPassword")
        );
        return Result.success("密码修改成功");
    }
}