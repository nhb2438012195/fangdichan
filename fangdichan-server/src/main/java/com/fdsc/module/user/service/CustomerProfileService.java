package com.fdsc.module.user.service;

import com.fdsc.module.user.entity.CustomerProfile;

public interface CustomerProfileService {
    CustomerProfile getProfile(Long userId);
    void updateProfile(Long userId, CustomerProfile profile);
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
