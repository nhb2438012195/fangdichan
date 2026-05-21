package com.fdsc.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fdsc.common.exception.BusinessException;
import com.fdsc.module.user.entity.CustomerProfile;
import com.fdsc.module.user.entity.SysUser;
import com.fdsc.module.user.mapper.CustomerProfileMapper;
import com.fdsc.module.user.mapper.UserMapper;
import com.fdsc.module.user.service.CustomerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerProfileServiceImpl implements CustomerProfileService {
    private final CustomerProfileMapper customerProfileMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CustomerProfile getProfile(Long userId) {
        CustomerProfile profile = customerProfileMapper.selectOne(
            new LambdaQueryWrapper<CustomerProfile>().eq(CustomerProfile::getUserId, userId));
        if (profile == null) throw new BusinessException(404, "个人信息不存在");
        return profile;
    }

    @Override
    public void updateProfile(Long userId, CustomerProfile profile) {
        CustomerProfile existing = customerProfileMapper.selectOne(
            new LambdaQueryWrapper<CustomerProfile>().eq(CustomerProfile::getUserId, userId));
        if (existing == null) throw new BusinessException(404, "个人信息不存在");
        profile.setId(existing.getId());
        profile.setUserId(userId);
        customerProfileMapper.update(profile, new LambdaQueryWrapper<CustomerProfile>().eq(CustomerProfile::getId, existing.getId()));
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
