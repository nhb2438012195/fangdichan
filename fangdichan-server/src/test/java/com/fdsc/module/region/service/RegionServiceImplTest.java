package com.fdsc.module.region.service;

import com.fdsc.module.region.entity.Region;
import com.fdsc.module.region.mapper.RegionMapper;
import com.fdsc.module.region.service.impl.RegionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegionServiceImplTest {
    @Mock private RegionMapper regionMapper;
    private RegionServiceImpl regionService;

    @BeforeEach
    void setUp() { regionService = new RegionServiceImpl(regionMapper); }

    @Test
    void getProvinces_shouldReturnLevel1Regions() {
        when(regionMapper.selectList(any())).thenReturn(List.of(
            createRegion(1L, "北京市", 1, null, "municipality"),
            createRegion(2L, "广东省", 1, null, "province")
        ));
        List<Region> result = regionService.getProvinces();
        assertEquals(2, result.size());
        verify(regionMapper).selectList(any());
    }

    @Test
    void getChildren_shouldReturnByParentId() {
        when(regionMapper.selectList(any())).thenReturn(List.of(
            createRegion(3L, "朝阳区", 2, 1L, "district")
        ));
        List<Region> result = regionService.getChildren(1L);
        assertEquals(1, result.size());
    }

    @Test
    void search_shouldReturnMatchingRegions() {
        when(regionMapper.selectList(any())).thenReturn(List.of(
            createRegion(3L, "广州市", 2, 2L, "city")
        ));
        List<Region> result = regionService.search("广州");
        assertEquals(1, result.size());
    }

    @Test
    void getById_shouldReturnRegion() {
        when(regionMapper.selectById(1L)).thenReturn(createRegion(1L, "北京市", 1, null, "municipality"));
        Region result = regionService.getById(1L);
        assertNotNull(result);
        assertEquals("北京市", result.getName());
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
