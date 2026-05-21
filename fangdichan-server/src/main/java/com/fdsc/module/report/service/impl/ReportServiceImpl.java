package com.fdsc.module.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fdsc.common.exception.BusinessException;
import com.fdsc.common.result.PageResult;
import com.fdsc.module.report.entity.Report;
import com.fdsc.module.report.mapper.ReportMapper;
import com.fdsc.module.report.service.ReportService;
import com.fdsc.module.property.entity.Property;
import com.fdsc.module.property.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ReportMapper reportMapper;
    private final PropertyMapper propertyMapper;

    @Override
    public void createReport(Long customerId, Long propertyId, String reason) {
        Report report = new Report();
        report.setCustomerId(customerId);
        report.setPropertyId(propertyId);
        report.setReason(reason);
        report.setStatus("PENDING");
        reportMapper.insert(report);
    }

    @Override
    public void dismissReport(Long adminId, Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) throw new BusinessException(404, "举报不存在");
        report.setStatus("DISMISSED");
        reportMapper.updateById(report);
    }

    @Override
    public void processReport(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) throw new BusinessException(404, "举报不存在");
        report.setStatus("PROCESSED");
        reportMapper.updateById(report);

        Property property = propertyMapper.selectById(report.getPropertyId());
        if (property != null) {
            property.setStatus("OFF_MARKET");
            propertyMapper.updateById(property);
        }
    }

    @Override
    public PageResult<?> listPending(int page, int size) {
        Page<Report> p = reportMapper.selectPage(new Page<>(page, size),
            new LambdaQueryWrapper<Report>().eq(Report::getStatus, "PENDING"));
        return PageResult.of(p.getRecords(), page, size, p.getTotal());
    }
}
