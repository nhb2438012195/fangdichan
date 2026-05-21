package com.fdsc.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("customer_profile")
public class CustomerProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String realName;
    private String phone;
    private String email;
    private String buyIntent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
