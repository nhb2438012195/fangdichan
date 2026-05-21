package com.fdsc.module.order.service;

import com.fdsc.common.exception.BusinessException;
import com.fdsc.module.order.entity.PurchaseOrder;
import com.fdsc.module.order.mapper.PurchaseOrderMapper;
import com.fdsc.module.order.service.impl.PurchaseOrderServiceImpl;
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
class PurchaseOrderServiceImplTest {

    @Mock private PurchaseOrderMapper purchaseOrderMapper;
    @Mock private PropertyMapper propertyMapper;
    private PurchaseOrderServiceImpl purchaseOrderService;

    @BeforeEach
    void setUp() {
        purchaseOrderService = new PurchaseOrderServiceImpl(purchaseOrderMapper, propertyMapper);
    }

    @Test
    void createOrder_shouldSucceed() {
        Property property = new Property();
        property.setId(1L);
        property.setStatus("APPROVED");
        when(propertyMapper.selectById(1L)).thenReturn(property);
        when(purchaseOrderMapper.selectCount(any())).thenReturn(0L);
        when(purchaseOrderMapper.insert(any())).thenReturn(1);

        purchaseOrderService.createOrder(100L, 1L, "我想购买");
        verify(purchaseOrderMapper).insert(any());
    }

    @Test
    void createOrder_shouldThrowWhenPropertyNotApproved() {
        Property property = new Property();
        property.setStatus("PENDING");
        when(propertyMapper.selectById(1L)).thenReturn(property);
        assertThrows(BusinessException.class,
            () -> purchaseOrderService.createOrder(100L, 1L, "消息"));
    }

    @Test
    void createOrder_shouldThrowWhenOtherOrderExists() {
        Property property = new Property();
        property.setId(1L);
        property.setStatus("APPROVED");
        when(propertyMapper.selectById(1L)).thenReturn(property);
        when(purchaseOrderMapper.selectCount(any())).thenReturn(1L);
        assertThrows(BusinessException.class,
            () -> purchaseOrderService.createOrder(100L, 1L, "消息"));
    }

    @Test
    void confirmOrder_shouldSucceed() {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(1L);
        order.setPropertyId(1L);
        order.setStatus("PENDING");
        when(purchaseOrderMapper.selectById(1L)).thenReturn(order);
        when(propertyMapper.selectById(1L)).thenReturn(new Property());

        purchaseOrderService.confirmOrder(10L, 1L);
        assertEquals("CONFIRMED", order.getStatus());
    }

    @Test
    void cancelOrder_shouldSucceed() {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(1L);
        order.setStatus("PENDING");
        order.setCustomerId(100L);
        when(purchaseOrderMapper.selectById(1L)).thenReturn(order);

        purchaseOrderService.cancelOrder(100L, 1L, "CUSTOMER");
        assertEquals("CANCELLED", order.getStatus());
    }
}
