package com.fdsc.module.report.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.company.entity.Company;
import com.fdsc.module.company.service.CompanyService;
import com.fdsc.module.report.service.ReportExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent/report")
@RequiredArgsConstructor
public class ReportExportController {
    private final ReportExportService reportExportService;
    private final CompanyService companyService;

    @GetMapping("/property-export")
    public ResponseEntity<byte[]> exportPropertyList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String roomType) {
        Company company = companyService.getCompanyByUserId(userId);
        byte[] data = reportExportService.exportPropertyList(company.getId(), district, roomType);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=房源报表.xlsx")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(data);
    }
}
