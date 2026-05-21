package com.fdsc.module.analysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fdsc.module.analysis.dto.VacancyAnalysisVO;
import com.fdsc.module.analysis.service.AnalysisService;
import com.fdsc.module.property.entity.Property;
import com.fdsc.module.property.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {
    private final PropertyMapper propertyMapper;

    @Override
    public VacancyAnalysisVO getVacancyAnalysis(Long companyId) {
        VacancyAnalysisVO vo = new VacancyAnalysisVO();
        vo.setDistrict(buildVacancyItems(companyId, "district"));
        vo.setFloor(buildVacancyItems(companyId, "floor"));
        vo.setRoomType(buildVacancyItems(companyId, "room_type"));
        return vo;
    }

    private List<VacancyAnalysisVO.VacancyItem> buildVacancyItems(Long companyId, String groupColumn) {
        List<Map<String, Object>> stats = propertyMapper.selectMaps(
            new QueryWrapper<Property>()
                .eq("company_id", companyId)
                .eq("status", "APPROVED")
                .select(groupColumn + " as name",
                    "COUNT(*) as total",
                    "SUM(CASE WHEN is_vacant = 1 THEN 1 ELSE 0 END) as vacant")
                .groupBy(groupColumn));
        return stats.stream().map(m -> {
            VacancyAnalysisVO.VacancyItem item = new VacancyAnalysisVO.VacancyItem();
            item.setName((String) m.get("name"));
            item.setTotal(((Number) m.get("total")).longValue());
            item.setVacant(((Number) m.get("vacant")).longValue());
            item.setVacancyRate(item.getTotal() > 0
                ? (double) item.getVacant() / item.getTotal() * 100 : 0);
            return item;
        }).collect(Collectors.toList());
    }
}
