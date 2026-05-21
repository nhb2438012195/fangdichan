package com.fdsc.module.config.service;

import com.fdsc.module.config.entity.SystemConfig;
import com.fdsc.module.config.mapper.SystemConfigMapper;
import com.fdsc.module.config.service.impl.SystemConfigServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemConfigServiceImplTest {
    @Mock private SystemConfigMapper systemConfigMapper;
    private SystemConfigServiceImpl systemConfigService;

    @BeforeEach
    void setUp() { systemConfigService = new SystemConfigServiceImpl(systemConfigMapper); }

    @Test
    void getConfig_shouldReturnValue() {
        SystemConfig cfg = new SystemConfig();
        cfg.setConfigValue("test-value");
        when(systemConfigMapper.selectOne(any())).thenReturn(cfg);
        assertEquals("test-value", systemConfigService.getConfig("test-key"));
    }

    @Test
    void getConfig_shouldReturnNullWhenNotFound() {
        when(systemConfigMapper.selectOne(any())).thenReturn(null);
        assertNull(systemConfigService.getConfig("not-exist"));
    }

    @Test
    void setConfig_shouldInsertWhenNew() {
        when(systemConfigMapper.selectOne(any())).thenReturn(null);
        systemConfigService.setConfig("new-key", "new-value", "描述");
        verify(systemConfigMapper).insert(any());
    }

    @Test
    void setConfig_shouldUpdateWhenExists() {
        SystemConfig existing = new SystemConfig();
        existing.setId(1L);
        when(systemConfigMapper.selectOne(any())).thenReturn(existing);
        systemConfigService.setConfig("key", "updated", "新描述");
        verify(systemConfigMapper).updateById(any());
    }

    @Test
    void listAll_shouldReturnList() {
        when(systemConfigMapper.selectList(any())).thenReturn(List.of(new SystemConfig()));
        assertEquals(1, systemConfigService.listAll().size());
    }
}
