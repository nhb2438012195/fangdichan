package com.fdsc.module.roomtype.controller;

import com.fdsc.module.roomtype.entity.RoomType;
import com.fdsc.module.roomtype.service.RoomTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomTypeControllerTest {
    @Mock private RoomTypeService roomTypeService;
    private RoomTypeController roomTypeController;

    @BeforeEach
    void setUp() { roomTypeController = new RoomTypeController(roomTypeService); }

    @Test
    void getRoomTypes_shouldReturnAll() {
        when(roomTypeService.getAll()).thenReturn(List.of(
            createRoomType(1L, 3, 2, "三室两厅")
        ));
        var result = roomTypeController.getRoomTypes(null);
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals(3, result.getData().get(0).getBedroomCount());
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
