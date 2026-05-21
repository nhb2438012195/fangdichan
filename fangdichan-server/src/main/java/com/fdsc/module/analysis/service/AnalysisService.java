package com.fdsc.module.analysis.service;

import com.fdsc.module.analysis.dto.VacancyAnalysisVO;

public interface AnalysisService {
    VacancyAnalysisVO getVacancyAnalysis(Long companyId);
}
