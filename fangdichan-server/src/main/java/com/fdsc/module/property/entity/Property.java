package com.fdsc.module.property.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Data
@TableName("property")
public class Property {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long companyId;
    private String title;
    private String location;
    private String district;
    private String floor;
    private Integer floorTotal;
    private String roomType;
    private BigDecimal area;
    private BigDecimal price;
    private BigDecimal unitPrice;
    private Boolean isVacant;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void setPrice(BigDecimal price) {
        this.price = price;
        if (this.area != null && price != null && this.area.compareTo(BigDecimal.ZERO) > 0) {
            this.unitPrice = price.divide(this.area, 2, RoundingMode.HALF_UP);
        }
    }

    public void setArea(BigDecimal area) {
        this.area = area;
        if (this.price != null && area != null && area.compareTo(BigDecimal.ZERO) > 0) {
            this.unitPrice = this.price.divide(area, 2, RoundingMode.HALF_UP);
        }
    }
}
