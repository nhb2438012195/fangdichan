package com.fdsc.module.user.service.impl;

import com.fdsc.common.exception.BusinessException;
import com.fdsc.module.user.dto.LoginRequest;
import com.fdsc.module.user.dto.LoginResponse;
import com.fdsc.module.user.entity.CustomerProfile;
import com.fdsc.module.user.entity.SysUser;
import com.fdsc.module.user.mapper.CustomerProfileMapper;
import com.fdsc.module.user.mapper.UserMapper;
import com.fdsc.module.user.service.UserService;
import com.fdsc.security.JwtTokenProvider;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final CustomerProfileMapper customerProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账户已被禁用");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());
        return new LoginResponse(token, user.getRole(), user.getId());
    }

    @Override
    public void register(LoginRequest request, String role) {
        SysUser existing = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (existing != null) {
            throw new BusinessException(400, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus(1);
        userMapper.insert(user);

        if ("CUSTOMER".equals(role)) {
            CustomerProfile profile = new CustomerProfile();
            profile.setUserId(user.getId());
            customerProfileMapper.insert(profile);
        }
    }
}
