package com.fdsc.module.user.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.user.dto.LoginRequest;
import com.fdsc.module.user.dto.LoginResponse;
import com.fdsc.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody LoginRequest request,
                              @RequestParam(defaultValue = "CUSTOMER") String role) {
        userService.register(request, role);
        return Result.success(null);
    }
}
