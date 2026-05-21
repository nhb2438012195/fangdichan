package com.fdsc.module.property.service;

import com.fdsc.common.exception.BusinessException;
import com.fdsc.common.result.PageResult;
import com.fdsc.module.property.entity.Property;
import com.fdsc.module.property.mapper.PropertyImageMapper;
import com.fdsc.module.property.mapper.PropertyMapper;
import com.fdsc.module.property.service.impl.PropertyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock private PropertyMapper propertyMapper;
    @Mock private PropertyImageMapper propertyImageMapper;
    @Mock private io.minio.MinioClient minioClient;

    private PropertyServiceImpl propertyService;

    @BeforeEach
    void setUp() {
        propertyService = new PropertyServiceImpl(propertyMapper, propertyImageMapper, minioClient);
    }

    @Test
    void createProperty_shouldSetPendingStatus() {
        Property property = new Property();
        property.setTitle("测试房源");
        property.setLocation("朝阳区");
        property.setRoomType("三室");
        property.setPrice(new BigDecimal("5000000"));
        property.setArea(new BigDecimal("100"));

        propertyService.createProperty(1L, property);
        assertEquals("PENDING", property.getStatus());
        assertEquals(1L, property.getCompanyId());
        // unitPrice = 5000000/100 = 50000.00
        assertEquals(0, new BigDecimal("50000.00").compareTo(property.getUnitPrice()));
        verify(propertyMapper).insert(property);
    }

    @Test
    void approveProperty_shouldChangeStatus() {
        Property property = new Property();
        property.setId(1L);
        property.setStatus("PENDING");
        when(propertyMapper.selectById(1L)).thenReturn(property);

        propertyService.approveProperty(10L, 1L);
        assertEquals("APPROVED", property.getStatus());
        verify(propertyMapper).updateById(property);
    }

    @Test
    void approveProperty_shouldThrowWhenNotPending() {
        Property property = new Property();
        property.setStatus("APPROVED");
        when(propertyMapper.selectById(1L)).thenReturn(property);
        assertThrows(BusinessException.class, () -> propertyService.approveProperty(10L, 1L));
    }

    @Test
    void rejectProperty_shouldChangeStatus() {
        Property property = new Property();
        property.setId(1L);
        property.setStatus("PENDING");
        when(propertyMapper.selectById(1L)).thenReturn(property);

        propertyService.rejectProperty(10L, 1L);
        assertEquals("REJECTED", property.getStatus());
    }

    @Test
    void setOffMarket_shouldChangeStatus() {
        Property property = new Property();
        property.setCompanyId(1L);
        property.setStatus("APPROVED");
        when(propertyMapper.selectById(1L)).thenReturn(property);

        propertyService.setOffMarket(1L, 1L);
        assertEquals("OFF_MARKET", property.getStatus());
    }

    @Test
    void setOffMarket_shouldThrowWhenWrongCompany() {
        Property property = new Property();
        property.setCompanyId(2L);
        when(propertyMapper.selectById(1L)).thenReturn(property);
        assertThrows(BusinessException.class, () -> propertyService.setOffMarket(1L, 1L));
    }

    @Test
    void search_shouldReturnResults() {
        Property p = new Property();
        p.setTitle("朝阳区好房");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Property> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        // Can't easily mock Page - use doReturn
        when(propertyMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Property> pg = invocation.getArgument(0);
            pg.setRecords(List.of(p));
            pg.setTotal(1);
            return pg;
        });

        PageResult<Property> result = propertyService.search("朝阳", null, null, null, null, null, null, 1, 10);
        assertEquals(1, result.getList().size());
    }

    @Test
    void createProperty_shouldThrowWhenPriceNegative() {
        Property property = new Property();
        property.setPrice(new BigDecimal("-100"));
        property.setArea(new BigDecimal("100"));
        assertThrows(BusinessException.class, () -> propertyService.createProperty(1L, property));
    }
}
