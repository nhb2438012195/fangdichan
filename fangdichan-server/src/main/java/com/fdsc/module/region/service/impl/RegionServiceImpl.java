package com.fdsc.module.region.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fdsc.module.region.entity.Region;
import com.fdsc.module.region.mapper.RegionMapper;
import com.fdsc.module.region.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {
    private final RegionMapper regionMapper;

    @Override
    public List<Region> getProvinces() {
        return regionMapper.selectList(
            new LambdaQueryWrapper<Region>().eq(Region::getLevel, 1));
    }

    @Override
    public List<Region> getChildren(Long parentId) {
        return regionMapper.selectList(
            new LambdaQueryWrapper<Region>().eq(Region::getParentId, parentId));
    }

    @Override
    public List<Region> search(String keyword) {
        return regionMapper.selectList(
            new LambdaQueryWrapper<Region>().like(Region::getName, keyword));
    }

    @Override
    public Region getById(Long id) {
        return regionMapper.selectById(id);
    }
}
