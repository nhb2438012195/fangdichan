package com.fdsc.module.config.service;

import com.fdsc.module.config.entity.SystemConfig;

import java.util.List;

public interface SystemConfigService {
    String getConfig(String key);
    void setConfig(String key, String value, String description);
    List<SystemConfig> listAll();
}
