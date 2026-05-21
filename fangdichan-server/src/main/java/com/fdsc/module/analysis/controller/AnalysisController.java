package com.fdsc.module.analysis.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.analysis.dto.VacancyAnalysisVO;
import com.fdsc.module.analysis.service.AnalysisService;
import com.fdsc.module.company.entity.Company;
import com.fdsc.module.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;
    private final CompanyService companyService;

    @GetMapping("/vacancy")
    public Result<VacancyAnalysisVO> getVacancyAnalysis(@AuthenticationPrincipal Long userId) {
        Company company = companyService.getCompanyByUserId(userId);
        return Result.success(analysisService.getVacancyAnalysis(company.getId()));
    }
}
