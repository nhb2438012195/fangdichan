package com.fdsc.module.user.service;

import com.fdsc.common.exception.BusinessException;
import com.fdsc.module.user.dto.LoginRequest;
import com.fdsc.module.user.entity.SysUser;
import com.fdsc.module.user.mapper.UserMapper;
import com.fdsc.module.user.mapper.CustomerProfileMapper;
import com.fdsc.module.user.service.impl.UserServiceImpl;
import com.fdsc.security.JwtTokenProvider;
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
class UserServiceImplTest {

    @Mock private UserMapper userMapper;
    @Mock private CustomerProfileMapper customerProfileMapper;
    @Mock private JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userMapper, customerProfileMapper, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void login_shouldSucceedWithValidCredentials() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setRole("ADMIN");
        user.setStatus(1);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(jwtTokenProvider.generateToken(1L, "ADMIN")).thenReturn("mock-token");

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");
        var response = userService.login(request);

        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void login_shouldThrowWithWrongPassword() {
        SysUser user = new SysUser();
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setStatus(1);
        when(userMapper.selectOne(any())).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");
        assertThrows(BusinessException.class, () -> userService.login(request));
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        when(userMapper.selectOne(any())).thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.setUsername("nobody");
        request.setPassword("pass");
        assertThrows(BusinessException.class, () -> userService.login(request));
    }

    @Test
    void login_shouldThrowWhenAccountDisabled() {
        SysUser user = new SysUser();
        user.setPassword(passwordEncoder.encode("pass123"));
        user.setStatus(0);
        when(userMapper.selectOne(any())).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("disabled");
        request.setPassword("pass123");
        assertThrows(BusinessException.class, () -> userService.login(request));
    }

    @Test
    void register_shouldThrowWhenUsernameExists() {
        when(userMapper.selectOne(any())).thenReturn(new SysUser());

        LoginRequest request = new LoginRequest();
        request.setUsername("existing");
        request.setPassword("pass123");
        assertThrows(BusinessException.class, () -> userService.register(request, "CUSTOMER"));
    }

    @Test
    void register_shouldCreateCustomerProfileForCustomerRole() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any())).thenReturn(1);

        LoginRequest request = new LoginRequest();
        request.setUsername("newuser");
        request.setPassword("pass123");
        userService.register(request, "CUSTOMER");

        verify(customerProfileMapper, times(1)).insert(any());
    }

    @Test
    void register_shouldNotCreateProfileForNonCustomer() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any())).thenReturn(1);

        LoginRequest request = new LoginRequest();
        request.setUsername("newagent");
        request.setPassword("pass123");
        userService.register(request, "AGENT");

        verify(customerProfileMapper, never()).insert(any());
    }
}
