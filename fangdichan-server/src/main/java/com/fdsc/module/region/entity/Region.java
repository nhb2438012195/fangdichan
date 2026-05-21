package com.fdsc.module.region.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("region")
public class Region {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer level;
    private Long parentId;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
