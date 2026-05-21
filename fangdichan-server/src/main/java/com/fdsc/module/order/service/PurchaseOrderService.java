package com.fdsc.module.order.service;

import com.fdsc.common.result.PageResult;
import com.fdsc.module.order.entity.PurchaseOrder;

public interface PurchaseOrderService {
    void createOrder(Long customerId, Long propertyId, String message);
    void confirmOrder(Long agentId, Long orderId);
    void cancelOrder(Long userId, Long orderId, String role);
    PageResult<PurchaseOrder> listByCustomer(Long customerId, int page, int size);
    PageResult<PurchaseOrder> listByCompany(Long companyId, int page, int size);
}
