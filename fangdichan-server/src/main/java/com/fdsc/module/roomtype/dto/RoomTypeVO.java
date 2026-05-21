package com.fdsc.module.roomtype.dto;

import lombok.Data;

@Data
public class RoomTypeVO {
    private Long id;
    private Integer bedroomCount;
    private Integer livingRoomCount;
    private String displayName;
}
