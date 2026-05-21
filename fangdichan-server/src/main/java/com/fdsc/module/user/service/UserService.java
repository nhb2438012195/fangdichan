package com.fdsc.module.user.service;

import com.fdsc.module.user.dto.LoginRequest;
import com.fdsc.module.user.dto.LoginResponse;

public interface UserService {
    LoginResponse login(LoginRequest request);
    void register(LoginRequest request, String role);
}
