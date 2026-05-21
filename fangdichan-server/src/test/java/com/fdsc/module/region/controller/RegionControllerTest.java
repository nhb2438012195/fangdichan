package com.fdsc.module.region.controller;

import com.fdsc.module.region.dto.RegionVO;
import com.fdsc.module.region.entity.Region;
import com.fdsc.module.region.service.RegionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegionControllerTest {
    @Mock private RegionService regionService;
    private RegionController regionController;

    @BeforeEach
    void setUp() { regionController = new RegionController(regionService); }

    @Test
    void getProvinces_shouldReturnRegionVOList() {
        when(regionService.getProvinces()).thenReturn(List.of(
            createRegion(1L, "北京市", 1, null, "municipality")
        ));
        var result = regionController.getProvinces();
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("北京市", result.getData().get(0).getName());
    }

    @Test
    void getChildren_shouldReturnChildren() {
        when(regionService.getChildren(1L)).thenReturn(List.of(
            createRegion(2L, "朝阳区", 2, 1L, "district")
        ));
        var result = regionController.getChildren(1L);
        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    void search_shouldReturnWithParentName() {
        Region guangzhou = createRegion(5L, "广州市", 2, 2L, "city");
        when(regionService.search("广州")).thenReturn(List.of(guangzhou));
        when(regionService.getById(2L)).thenReturn(createRegion(2L, "广东省", 1, null, "province"));

        var result = regionController.search("广州");
        assertEquals(200, result.getCode());
        assertEquals("广东省", result.getData().get(0).getParentName());
    }

    private Region createRegion(Long id, String name, Integer level, Long parentId, String type) {
        Region r = new Region();
        r.setId(id);
        r.setName(name);
        r.setLevel(level);
        r.setParentId(parentId);
        r.setType(type);
        return r;
    }
}
