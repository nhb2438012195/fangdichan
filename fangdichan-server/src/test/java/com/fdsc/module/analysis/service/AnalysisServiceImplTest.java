package com.fdsc.module.analysis.service;

import com.fdsc.module.analysis.dto.VacancyAnalysisVO;
import com.fdsc.module.analysis.service.impl.AnalysisServiceImpl;
import com.fdsc.module.property.mapper.PropertyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceImplTest {
    @Mock private PropertyMapper propertyMapper;
    private AnalysisServiceImpl analysisService;

    @BeforeEach
    void setUp() { analysisService = new AnalysisServiceImpl(propertyMapper); }

    @Test
    void getVacancyAnalysis_shouldReturnGroupedData() {
        when(propertyMapper.selectMaps(any())).thenReturn(List.of(
            Map.of("name", "朝阳区", "total", 10L, "vacant", 3L)
        ));

        VacancyAnalysisVO result = analysisService.getVacancyAnalysis(1L);
        assertNotNull(result);
        verify(propertyMapper, atLeast(3)).selectMaps(any());
    }
}
