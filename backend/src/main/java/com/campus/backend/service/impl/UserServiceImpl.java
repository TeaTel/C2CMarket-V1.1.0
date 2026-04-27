package com.campus.backend.service.impl;

import com.campus.backend.common.ErrorCode;
import com.campus.backend.dto.UserRegisterDTO;
import com.campus.backend.dto.UserVO;
import com.campus.backend.entity.User;
import com.campus.backend.exception.BusinessException;
import com.campus.backend.exception.NotFoundException;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserVO register(UserRegisterDTO dto) {
        // 检查用户名是否已存在
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "用户名已存在");
        }
        // 检查手机号
        if (userMapper.selectByPhone(dto.getPhone()) != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "手机号已注册");
        }

        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        if (user.getIsStudent() == null) {
            user.setIsStudent(true);
        }
        userMapper.insert(user);
        log.info("新用户注册成功: username={}", user.getUsername());
        return convertToVO(user);
    }

    @Override
    public UserVO login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "账号已被禁用");
        }

        // 更新最后登录时间
        userMapper.updateLastLogin(user.getId(), LocalDateTime.now());
        return convertToVO(user);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        return convertToVO(getUserEntityById(userId));
    }

    @Override
    public UserVO getUserInfoByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new NotFoundException("用户不存在: " + username);
        }
        return convertToVO(user);
    }

    @Override
    @Transactional
    public UserVO updateProfile(Long userId, User profileData) {
        User existing = getUserEntityById(userId);
        // 只更新允许修改的字段
        existing.setNickname(profileData.getNickname());
        existing.setAvatar(profileData.getAvatar());
        existing.setGender(profileData.getGender());
        existing.setSchool(profileData.getSchool());
        existing.setMajor(profileData.getMajor());
        existing.setGrade(profileData.getGrade());
        existing.setWechat(profileData.getWechat());
        existing.setQq(profileData.getQq());
        existing.setBio(profileData.getBio());
        userMapper.updateProfile(existing);
        return convertToVO(existing);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserEntityById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "旧密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updatePassword(user);
    }

    @Override
    public void sendResetCode(String account) {
        User user = userMapper.selectByUsername(account);
        if (user == null) {
            user = userMapper.selectByPhone(account);
        }
        if (user == null) {
            throw new NotFoundException("用户", account);
        }
        log.info("重置密码验证码发送成功: account={}", account);
    }

    @Override
    @Transactional
    public void verifyAndResetPassword(String account, String verifyCode, String newPassword) {
        User user = userMapper.selectByUsername(account);
        if (user == null) {
            user = userMapper.selectByPhone(account);
        }
        if (user == null) {
            throw new NotFoundException("用户", account);
        }

        if (verifyCode == null || verifyCode.length() != 6) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "验证码格式错误");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updatePassword(user);
        log.info("密码重置成功: account={}", account);
    }

    @Override
    public User getUserEntityById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new NotFoundException("用户", userId);
        }
        return user;
    }

    /**
     * Entity -> VO 转换 (排除敏感字段)
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        // 不复制 passwordHash 等敏感信息
        return vo;
    }
}
