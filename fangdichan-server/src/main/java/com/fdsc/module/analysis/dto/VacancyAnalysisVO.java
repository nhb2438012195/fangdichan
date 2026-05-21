package com.fdsc.module.analysis.dto;

import lombok.Data;
import java.util.List;

@Data
public class VacancyAnalysisVO {
    private List<VacancyItem> district;
    private List<VacancyItem> floor;
    private List<VacancyItem> roomType;

    @Data
    public static class VacancyItem {
        private String name;
        private long total;
        private long vacant;
        private double vacancyRate;
    }
}
