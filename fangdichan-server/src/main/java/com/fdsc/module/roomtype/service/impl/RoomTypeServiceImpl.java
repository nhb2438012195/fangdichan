package com.fdsc.module.roomtype.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fdsc.module.roomtype.entity.RoomType;
import com.fdsc.module.roomtype.mapper.RoomTypeMapper;
import com.fdsc.module.roomtype.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeServiceImpl implements RoomTypeService {
    private final RoomTypeMapper roomTypeMapper;

    @Override
    public List<RoomType> getAll() {
        return roomTypeMapper.selectList(null);
    }
}
