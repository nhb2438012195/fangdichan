package com.fdsc.module.region.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.region.dto.RegionVO;
import com.fdsc.module.region.entity.Region;
import com.fdsc.module.region.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/region")
@RequiredArgsConstructor
public class RegionController {
    private final RegionService regionService;

    @GetMapping("/provinces")
    public Result<List<RegionVO>> getProvinces() {
        return Result.success(toVOList(regionService.getProvinces()));
    }

    @GetMapping("/children")
    public Result<List<RegionVO>> getChildren(@RequestParam Long parentId) {
        return Result.success(toVOList(regionService.getChildren(parentId)));
    }

    @GetMapping("/search")
    public Result<List<RegionVO>> search(@RequestParam String keyword) {
        List<Region> regions = regionService.search(keyword);
        List<RegionVO> vos = regions.stream().map(r -> {
            RegionVO vo = toVO(r);
            if (r.getParentId() != null) {
                Region parent = regionService.getById(r.getParentId());
                if (parent != null) vo.setParentName(parent.getName());
            }
            return vo;
        }).collect(Collectors.toList());
        return Result.success(vos);
    }

    private List<RegionVO> toVOList(List<Region> regions) {
        return regions.stream().map(this::toVO).collect(Collectors.toList());
    }

    private RegionVO toVO(Region r) {
        RegionVO vo = new RegionVO();
        vo.setId(r.getId());
        vo.setName(r.getName());
        vo.setLevel(r.getLevel());
        vo.setType(r.getType());
        return vo;
    }
}
