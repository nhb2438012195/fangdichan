package com.fdsc.module.property.controller;

import com.fdsc.common.result.Result;
import com.fdsc.common.result.PageResult;
import com.fdsc.module.property.entity.Property;
import com.fdsc.module.property.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/agent/property")
@RequiredArgsConstructor
public class AgentPropertyController {
    private final PropertyService propertyService;

    @PostMapping
    public Result<?> create(@AuthenticationPrincipal Long userId, @RequestBody Property property) {
        propertyService.createProperty(userId, property);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    public Result<?> update(@AuthenticationPrincipal Long userId, @PathVariable Long id, @RequestBody Property property) {
        propertyService.updateProperty(userId, id, property);
        return Result.success(null);
    }

    @PutMapping("/{id}/off-market")
    public Result<?> setOffMarket(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        propertyService.setOffMarket(userId, id);
        return Result.success(null);
    }

    @GetMapping("/list")
    public Result<PageResult<Property>> list(@AuthenticationPrincipal Long userId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.success(propertyService.listByCompany(userId, page, size));
    }

    @PostMapping("/image/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.success(propertyService.uploadImage(file));
    }

    @DeleteMapping("/image/{id}")
    public Result<?> deleteImage(@PathVariable Long id) {
        propertyService.deleteImage(id);
        return Result.success(null);
    }
}
