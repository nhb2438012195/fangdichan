package com.fdsc.module.report.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
public class AdminReportController {
    private final ReportService reportService;

    @GetMapping("/pending")
    public Result<?> listPending(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return Result.success(reportService.listPending(page, size));
    }

    @PutMapping("/{id}/dismiss")
    public Result<?> dismiss(@AuthenticationPrincipal Long adminId, @PathVariable Long id) {
        reportService.dismissReport(adminId, id);
        return Result.success(null);
    }

    @PutMapping("/{id}/process")
    public Result<?> process(@PathVariable Long id) {
        reportService.processReport(id);
        return Result.success(null);
    }
}
