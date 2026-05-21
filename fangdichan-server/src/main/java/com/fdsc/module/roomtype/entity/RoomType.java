package com.fdsc.module.roomtype.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("room_type")
public class RoomType {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer bedroomCount;
    private Integer livingRoomCount;
    private String displayName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
