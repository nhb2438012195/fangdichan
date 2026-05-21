package com.fdsc.module.report.service;

import com.fdsc.module.report.entity.Report;
import com.fdsc.module.report.mapper.ReportMapper;
import com.fdsc.module.report.service.impl.ReportServiceImpl;
import com.fdsc.module.property.entity.Property;
import com.fdsc.module.property.mapper.PropertyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {
    @Mock private ReportMapper reportMapper;
    @Mock private PropertyMapper propertyMapper;
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() { reportService = new ReportServiceImpl(reportMapper, propertyMapper); }

    @Test
    void createReport_shouldInsertWithPending() {
        reportService.createReport(1L, 1L, "虚假信息");
        verify(reportMapper).insert(any());
    }

    @Test
    void processReport_shouldSetProcessed() {
        Report report = new Report();
        report.setPropertyId(1L);
        when(reportMapper.selectById(1L)).thenReturn(report);
        when(propertyMapper.selectById(1L)).thenReturn(new Property());

        reportService.processReport(1L);
        assertEquals("PROCESSED", report.getStatus());
    }
}
