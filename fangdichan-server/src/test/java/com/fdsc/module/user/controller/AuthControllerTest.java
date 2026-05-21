package com.fdsc.module.user.controller;

import com.fdsc.module.user.dto.LoginRequest;
import com.fdsc.module.user.dto.LoginResponse;
import com.fdsc.module.user.service.UserService;
import com.fdsc.security.JwtAuthenticationFilter;
import com.fdsc.security.JwtTokenProvider;
import com.fdsc.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserService userService;
    @SpyBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void login_shouldReturn200() throws Exception {
        when(userService.login(any())).thenReturn(new LoginResponse("token", "ADMIN", 1L));

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        mockMvc.perform(post("/api/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void register_shouldReturn200() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("newuser");
        request.setPassword("pass123");

        mockMvc.perform(post("/api/public/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
