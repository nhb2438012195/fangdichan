package com.fdsc.module.report.service;

import com.fdsc.common.result.PageResult;

public interface ReportService {
    void createReport(Long customerId, Long propertyId, String reason);
    void dismissReport(Long adminId, Long reportId);
    void processReport(Long reportId);
    PageResult<?> listPending(int page, int size);
}
