package com.fdsc.module.roomtype.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.roomtype.dto.RoomTypeVO;
import com.fdsc.module.roomtype.entity.RoomType;
import com.fdsc.module.roomtype.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/room-types")
@RequiredArgsConstructor
public class RoomTypeController {
    private final RoomTypeService roomTypeService;

    @GetMapping
    public Result<List<RoomTypeVO>> getRoomTypes(
            @RequestParam(required = false) Long regionId) {
        List<RoomType> list = roomTypeService.getAll();
        return Result.success(list.stream().map(this::toVO).collect(Collectors.toList()));
    }

    private RoomTypeVO toVO(RoomType rt) {
        RoomTypeVO vo = new RoomTypeVO();
        vo.setId(rt.getId());
        vo.setBedroomCount(rt.getBedroomCount());
        vo.setLivingRoomCount(rt.getLivingRoomCount());
        vo.setDisplayName(rt.getDisplayName());
        return vo;
    }
}
