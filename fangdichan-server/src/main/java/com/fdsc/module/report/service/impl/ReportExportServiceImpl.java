package com.fdsc.module.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fdsc.common.exception.BusinessException;
import com.fdsc.module.property.entity.Property;
import com.fdsc.module.property.mapper.PropertyMapper;
import com.fdsc.module.report.service.ReportExportService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportExportServiceImpl implements ReportExportService {
    private final PropertyMapper propertyMapper;

    @Override
    public byte[] exportPropertyList(Long companyId, String district, String roomType) {
        LambdaQueryWrapper<Property> qw = new LambdaQueryWrapper<Property>()
            .eq(Property::getCompanyId, companyId);
        if (district != null && !district.isEmpty()) qw.eq(Property::getDistrict, district);
        if (roomType != null && !roomType.isEmpty()) qw.eq(Property::getRoomType, roomType);

        List<Property> list = propertyMapper.selectList(qw);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("房源列表");
            Row header = sheet.createRow(0);
            String[] cols = {"标题", "区域", "户型", "面积", "价格", "状态"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

            int rowIdx = 1;
            for (Property p : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getTitle());
                row.createCell(1).setCellValue(p.getDistrict());
                row.createCell(2).setCellValue(p.getRoomType());
                row.createCell(3).setCellValue(p.getArea() != null ? p.getArea().doubleValue() : 0);
                row.createCell(4).setCellValue(p.getPrice() != null ? p.getPrice().doubleValue() : 0);
                row.createCell(5).setCellValue(p.getStatus());
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException(500, "导出失败");
        }
    }

    @Override
    public byte[] exportAnalysisReport(Long companyId) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("关联分析");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("维度");
            header.createCell(1).setCellValue("名称");
            header.createCell(2).setCellValue("总数");
            header.createCell(3).setCellValue("空置数");
            header.createCell(4).setCellValue("空置率(%)");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException(500, "导出失败");
        }
    }
}
