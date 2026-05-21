package com.fdsc.module.company.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.company.entity.Company;
import com.fdsc.module.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent/company")
@RequiredArgsConstructor
public class AgentCompanyController {
    private final CompanyService companyService;

    @GetMapping
    public Result<Company> getMyCompany(@AuthenticationPrincipal Long userId) {
        return Result.success(companyService.getCompanyByUserId(userId));
    }

    @PutMapping
    public Result<?> updateCompany(@AuthenticationPrincipal Long userId, @RequestBody Company company) {
        companyService.updateCompany(userId, company);
        return Result.success(null);
    }
}
