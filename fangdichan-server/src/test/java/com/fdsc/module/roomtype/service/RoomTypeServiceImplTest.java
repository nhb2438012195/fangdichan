package com.fdsc.module.roomtype.service;

import com.fdsc.module.roomtype.entity.RoomType;
import com.fdsc.module.roomtype.mapper.RoomTypeMapper;
import com.fdsc.module.roomtype.service.impl.RoomTypeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomTypeServiceImplTest {
    @Mock private RoomTypeMapper roomTypeMapper;
    private RoomTypeServiceImpl roomTypeService;

    @BeforeEach
    void setUp() { roomTypeService = new RoomTypeServiceImpl(roomTypeMapper); }

    @Test
    void getAll_shouldReturnAllRoomTypes() {
        when(roomTypeMapper.selectList(any())).thenReturn(List.of(
            createRoomType(1L, 3, 2, "三室两厅")
        ));
        List<RoomType> result = roomTypeService.getAll();
        assertEquals(1, result.size());
        assertEquals("三室两厅", result.get(0).getDisplayName());
    }

    private RoomType createRoomType(Long id, int bedroom, int livingRoom, String displayName) {
        RoomType rt = new RoomType();
        rt.setId(id);
        rt.setBedroomCount(bedroom);
        rt.setLivingRoomCount(livingRoom);
        rt.setDisplayName(displayName);
        return rt;
    }
}
