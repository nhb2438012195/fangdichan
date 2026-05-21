package com.fdsc.module.property.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.property.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/property")
@RequiredArgsConstructor
public class AdminPropertyController {
    private final PropertyService propertyService;

    @PutMapping("/{id}/approve")
    public Result<?> approve(@AuthenticationPrincipal Long adminId, @PathVariable Long id) {
        propertyService.approveProperty(adminId, id);
        return Result.success(null);
    }

    @PutMapping("/{id}/reject")
    public Result<?> reject(@AuthenticationPrincipal Long adminId, @PathVariable Long id) {
        propertyService.rejectProperty(adminId, id);
        return Result.success(null);
    }
}
