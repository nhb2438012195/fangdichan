package com.fdsc.module.report.service;

public interface ReportExportService {
    byte[] exportPropertyList(Long companyId, String district, String roomType);
    byte[] exportAnalysisReport(Long companyId);
}
