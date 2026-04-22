package com.campus.backend.service.impl;

import com.campus.backend.common.ErrorCode;
import com.campus.backend.dto.UserRegisterDTO;
import com.campus.backend.dto.UserVO;
import com.campus.backend.entity.User;
import com.campus.backend.exception.BusinessException;
import com.campus.backend.exception.NotFoundException;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserVO register(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(registerDTO.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "用户名已存在");
        }
        
        // 检查手机号是否已存在
        existingUser = userMapper.selectByPhone(registerDTO.getPhone());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "手机号已注册");
        }
        
        // 创建用户实体
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPasswordHash(passwordEncoder.encode(registerDTO.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // 设置默认头像
        if (user.getAvatar() == null) {
            user.setAvatar("");
        }
        
        // 插入数据库
        userMapper.insert(user);
        
        // 返回用户信息
        return convertToVO(user);
    }

    @Override
    public UserVO login(String username, String password) {
        // 查找用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "用户名或密码错误");
        }
        
        // 如果提供了密码，验证密码
        if (password != null && !password.isEmpty()) {
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "用户名或密码错误");
            }
        }
        
        // 检查用户状态
        if ("banned".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "账号已被封禁");
        }
        
        return convertToVO(user);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new NotFoundException("用户", userId);
        }
        return convertToVO(user);
    }

    @Override
    public UserVO getUserInfoByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("旧密码错误");
        }
        
        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updatePassword(user);
    }

    @Override
    @Transactional
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setAvatar(avatarUrl);
        userMapper.updateAvatar(user);
    }
    
    private UserVO convertToVO(User user) {
        UserVO userVO = new UserVO();
        // 手动复制字段，避免密码等敏感信息泄露
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setPhone(user.getPhone());
        userVO.setWechat(user.getWechat());
        userVO.setQq(user.getQq());
        userVO.setAvatar(user.getAvatar());
        userVO.setSchool(user.getSchool());
        userVO.setMajor(user.getMajor());
        userVO.setIsStudent(user.getIsStudent());
        userVO.setStatus(user.getStatus());
        userVO.setCreatedAt(user.getCreatedAt());
        // 注意：不复制passwordHash字段，这是敏感信息
        return userVO;
    }
}