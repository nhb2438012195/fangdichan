package com.fdsc.module.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fdsc.module.config.entity.SystemConfig;
import com.fdsc.module.config.mapper.SystemConfigMapper;
import com.fdsc.module.config.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {
    private final SystemConfigMapper systemConfigMapper;

    @Override
    public String getConfig(String key) {
        SystemConfig cfg = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        return cfg != null ? cfg.getConfigValue() : null;
    }

    @Override
    public void setConfig(String key, String value, String description) {
        SystemConfig existing = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        if (existing != null) {
            existing.setConfigValue(value);
            if (description != null) existing.setDescription(description);
            systemConfigMapper.updateById(existing);
        } else {
            SystemConfig cfg = new SystemConfig();
            cfg.setConfigKey(key);
            cfg.setConfigValue(value);
            cfg.setDescription(description);
            systemConfigMapper.insert(cfg);
        }
    }

    @Override
    public List<SystemConfig> listAll() {
        return systemConfigMapper.selectList(null);
    }
}
