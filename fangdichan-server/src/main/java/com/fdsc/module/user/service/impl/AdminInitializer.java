package com.fdsc.module.user.service.impl;

import com.fdsc.module.user.entity.SysUser;
import com.fdsc.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        SysUser admin = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "admin"));
        if (admin != null && "PLACEHOLDER".equals(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode("admin123"));
            userMapper.updateById(admin);
            System.out.println("✅ 管理员初始密码已初始化 (admin/admin123)");
        }
    }
}
