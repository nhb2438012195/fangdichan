package com.fdsc.module.order.controller;

import com.fdsc.common.result.Result;
import com.fdsc.common.result.PageResult;
import com.fdsc.module.order.entity.PurchaseOrder;
import com.fdsc.module.order.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent/order")
@RequiredArgsConstructor
public class AgentOrderController {
    private final PurchaseOrderService purchaseOrderService;

    @GetMapping("/list")
    public Result<PageResult<PurchaseOrder>> list(@AuthenticationPrincipal Long userId,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return Result.success(purchaseOrderService.listByCompany(userId, page, size));
    }

    @PutMapping("/{id}/confirm")
    public Result<?> confirm(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        purchaseOrderService.confirmOrder(userId, id);
        return Result.success(null);
    }

    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        purchaseOrderService.cancelOrder(userId, id, "AGENT");
        return Result.success(null);
    }
}
