package com.campus.backend.service;

import com.campus.backend.dto.UserRegisterDTO;
import com.campus.backend.dto.UserVO;
import com.campus.backend.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface UserService {
    
    UserVO register(UserRegisterDTO registerDTO);
    
    UserVO login(String username, String password);
    
    UserVO getUserInfo(Long userId);
    
    UserVO getUserInfoByUsername(String username);
    
    void updatePassword(Long userId, String oldPassword, String newPassword);
    
    void updateAvatar(Long userId, String avatarUrl);
}