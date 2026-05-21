package com.fdsc.module.property.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("property_image")
public class PropertyImage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long propertyId;
    private String imageUrl;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
