package com.fdsc.module.region.dto;

import lombok.Data;

@Data
public class RegionVO {
    private Long id;
    private String name;
    private Integer level;
    private String type;
    private String parentName;
}
