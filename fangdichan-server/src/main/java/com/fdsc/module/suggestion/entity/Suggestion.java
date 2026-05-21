package com.fdsc.module.suggestion.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("suggestion")
public class Suggestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;
    private Long companyId;
    private String desiredType;
    private BigDecimal desiredPriceMin;
    private BigDecimal desiredPriceMax;
    private String content;
    private LocalDateTime createdAt;
}
