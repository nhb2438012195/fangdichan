package com.fdsc.module.order.controller;

import com.fdsc.common.result.Result;
import com.fdsc.common.result.PageResult;
import com.fdsc.module.order.entity.PurchaseOrder;
import com.fdsc.module.order.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/order")
@RequiredArgsConstructor
public class CustomerOrderController {
    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public Result<?> createOrder(@AuthenticationPrincipal Long userId,
                                  @RequestParam Long propertyId,
                                  @RequestParam(required = false) String message) {
        purchaseOrderService.createOrder(userId, propertyId, message);
        return Result.success(null);
    }

    @GetMapping("/list")
    public Result<PageResult<PurchaseOrder>> list(@AuthenticationPrincipal Long userId,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return Result.success(purchaseOrderService.listByCustomer(userId, page, size));
    }

    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        purchaseOrderService.cancelOrder(userId, id, "CUSTOMER");
        return Result.success(null);
    }
}
