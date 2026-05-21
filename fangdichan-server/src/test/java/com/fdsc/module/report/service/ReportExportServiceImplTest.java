package com.fdsc.module.report.service;

import com.fdsc.module.property.entity.Property;
import com.fdsc.module.property.mapper.PropertyMapper;
import com.fdsc.module.report.service.impl.ReportExportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReportExportServiceImplTest {
    @Mock private PropertyMapper propertyMapper;
    private ReportExportServiceImpl reportExportService;

    @BeforeEach
    void setUp() { reportExportService = new ReportExportServiceImpl(propertyMapper); }

    @Test
    void exportPropertyList_shouldReturnNonEmptyExcel() {
        when(propertyMapper.selectList(any())).thenReturn(List.of(
            new Property() {{ setTitle("房源A"); setPrice(new BigDecimal("100")); setStatus("APPROVED"); }},
            new Property() {{ setTitle("房源B"); setPrice(new BigDecimal("200")); setStatus("APPROVED"); }}
        ));

        byte[] data = reportExportService.exportPropertyList(1L, null, null);
        assertNotNull(data);
        assertTrue(data.length > 0);
        // Verify Excel magic number (PK\x03\x04)
        assertArrayEquals(new byte[]{0x50, 0x4B, 0x03, 0x04}, Arrays.copyOf(data, 4));
    }

    @Test
    void exportAnalysisReport_shouldReturnNonEmptyExcel() {
        byte[] data = reportExportService.exportAnalysisReport(1L);
        assertNotNull(data);
        assertTrue(data.length > 0);
    }
}
