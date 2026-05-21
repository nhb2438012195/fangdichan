package com.fdsc.module.message.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("conversation")
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerId;
    private Long companyId;
    private Long propertyId;
    private String status;
    private Integer customerUnread;
    private Integer agentUnread;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
