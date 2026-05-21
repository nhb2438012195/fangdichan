package com.fdsc.module.report.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/report")
@RequiredArgsConstructor
public class CustomerReportController {
    private final ReportService reportService;

    @PostMapping
    public Result<?> create(@AuthenticationPrincipal Long userId,
                             @RequestParam Long propertyId,
                             @RequestParam String reason) {
        reportService.createReport(userId, propertyId, reason);
        return Result.success(null);
    }
}
