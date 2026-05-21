package com.fdsc.module.user.service;

import com.fdsc.common.exception.BusinessException;
import com.fdsc.module.user.entity.CustomerProfile;
import com.fdsc.module.user.entity.SysUser;
import com.fdsc.module.user.mapper.CustomerProfileMapper;
import com.fdsc.module.user.mapper.UserMapper;
import com.fdsc.module.user.service.impl.CustomerProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceImplTest {

    @Mock private CustomerProfileMapper customerProfileMapper;
    @Mock private UserMapper userMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private CustomerProfileServiceImpl customerProfileService;

    @BeforeEach
    void setUp() {
        customerProfileService = new CustomerProfileServiceImpl(customerProfileMapper, userMapper, passwordEncoder);
    }

    @Test
    void getProfile_shouldReturnProfile() {
        CustomerProfile profile = new CustomerProfile();
        profile.setUserId(1L);
        profile.setRealName("张三");
        when(customerProfileMapper.selectOne(any())).thenReturn(profile);

        CustomerProfile result = customerProfileService.getProfile(1L);
        assertNotNull(result);
        assertEquals("张三", result.getRealName());
    }

    @Test
    void getProfile_shouldThrowWhenNotFound() {
        when(customerProfileMapper.selectOne(any())).thenReturn(null);
        assertThrows(BusinessException.class, () -> customerProfileService.getProfile(1L));
    }

    @Test
    void updateProfile_shouldSucceed() {
        CustomerProfile profile = new CustomerProfile();
        profile.setRealName("李四");
        profile.setPhone("13800138000");
        when(customerProfileMapper.selectOne(any())).thenReturn(new CustomerProfile());

        customerProfileService.updateProfile(1L, profile);
        verify(customerProfileMapper).update(any(), any());
    }

    @Test
    void updatePassword_shouldSucceed() {
        SysUser user = new SysUser();
        user.setPassword(passwordEncoder.encode("oldPass"));
        when(userMapper.selectById(1L)).thenReturn(user);

        customerProfileService.updatePassword(1L, "oldPass", "newPass");
        verify(userMapper).updateById(any());
    }

    @Test
    void updatePassword_shouldThrowWhenOldPasswordWrong() {
        SysUser user = new SysUser();
        user.setPassword(passwordEncoder.encode("correctOld"));
        when(userMapper.selectById(1L)).thenReturn(user);

        assertThrows(BusinessException.class,
            () -> customerProfileService.updatePassword(1L, "wrongOld", "newPass"));
    }
}
