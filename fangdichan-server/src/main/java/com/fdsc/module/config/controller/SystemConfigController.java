package com.fdsc.module.config.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.config.entity.SystemConfig;
import com.fdsc.module.config.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class SystemConfigController {
    private final SystemConfigService systemConfigService;

    @GetMapping
    public Result<List<SystemConfig>> listAll() {
        return Result.success(systemConfigService.listAll());
    }

    @PutMapping("/{key}")
    public Result<?> updateConfig(@PathVariable String key, @RequestParam String value,
                                   @RequestParam(required = false) String description) {
        systemConfigService.setConfig(key, value, description);
        return Result.success(null);
    }
}
