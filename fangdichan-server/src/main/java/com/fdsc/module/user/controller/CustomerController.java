package com.fdsc.module.user.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.user.entity.CustomerProfile;
import com.fdsc.module.user.service.CustomerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/profile")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerProfileService customerProfileService;

    @GetMapping
    public Result<CustomerProfile> getProfile(@AuthenticationPrincipal Long userId) {
        return Result.success(customerProfileService.getProfile(userId));
    }

    @PutMapping
    public Result<?> updateProfile(@AuthenticationPrincipal Long userId, @RequestBody CustomerProfile profile) {
        customerProfileService.updateProfile(userId, profile);
        return Result.success(null);
    }

    @PutMapping("/password")
    public Result<?> updatePassword(@AuthenticationPrincipal Long userId,
                                     @RequestParam String oldPassword,
                                     @RequestParam String newPassword) {
        customerProfileService.updatePassword(userId, oldPassword, newPassword);
        return Result.success(null);
    }
}
