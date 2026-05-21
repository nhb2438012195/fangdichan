package com.fdsc.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fdsc.common.exception.BusinessException;
import com.fdsc.common.result.PageResult;
import com.fdsc.module.order.entity.PurchaseOrder;
import com.fdsc.module.order.mapper.PurchaseOrderMapper;
import com.fdsc.module.order.service.PurchaseOrderService;
import com.fdsc.module.property.entity.Property;
import com.fdsc.module.property.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PropertyMapper propertyMapper;

    @Override
    public void createOrder(Long customerId, Long propertyId, String message) {
        Property property = propertyMapper.selectById(propertyId);
        if (property == null || !"APPROVED".equals(property.getStatus())) {
            throw new BusinessException(400, "该房源不可购买");
        }
        Long count = purchaseOrderMapper.selectCount(
            new LambdaQueryWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getPropertyId, propertyId)
                .in(PurchaseOrder::getStatus, "PENDING", "CONFIRMED"));
        if (count > 0) {
            throw new BusinessException(400, "该房源已有客户在交易中");
        }
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(generateOrderNo());
        order.setCustomerId(customerId);
        order.setPropertyId(propertyId);
        order.setStatus("PENDING");
        order.setMessage(message);
        purchaseOrderMapper.insert(order);
    }

    @Override
    public void confirmOrder(Long agentId, Long orderId) {
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null || !"PENDING".equals(order.getStatus())) {
            throw new BusinessException(400, "订单状态异常");
        }
        order.setStatus("CONFIRMED");
        purchaseOrderMapper.updateById(order);

        Property property = propertyMapper.selectById(order.getPropertyId());
        if (property != null) {
            property.setIsVacant(false);
            property.setStatus("SOLD");
            property.setUnitPrice(null);
            if (property.getPrice() != null && property.getArea() != null && property.getArea().compareTo(java.math.BigDecimal.ZERO) > 0) {
                property.setUnitPrice(property.getPrice().divide(property.getArea(), 2, java.math.RoundingMode.HALF_UP));
            }
            propertyMapper.updateById(property);
        }
    }

    @Override
    public void cancelOrder(Long userId, Long orderId, String role) {
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "订单不存在");
        if ("CUSTOMER".equals(role) && !order.getCustomerId().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }
        order.setStatus("CANCELLED");
        purchaseOrderMapper.updateById(order);
    }

    @Override
    public PageResult<PurchaseOrder> listByCustomer(Long customerId, int page, int size) {
        Page<PurchaseOrder> p = purchaseOrderMapper.selectPage(new Page<>(page, size),
            new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getCustomerId, customerId));
        return PageResult.of(p.getRecords(), page, size, p.getTotal());
    }

    @Override
    public PageResult<PurchaseOrder> listByCompany(Long companyId, int page, int size) {
        Page<PurchaseOrder> p = purchaseOrderMapper.selectPage(new Page<>(page, size),
            new LambdaQueryWrapper<PurchaseOrder>());
        return PageResult.of(p.getRecords(), page, size, p.getTotal());
    }

    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
