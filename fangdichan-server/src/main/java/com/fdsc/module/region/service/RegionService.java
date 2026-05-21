package com.fdsc.module.region.service;

import com.fdsc.module.region.entity.Region;
import java.util.List;

public interface RegionService {
    List<Region> getProvinces();
    List<Region> getChildren(Long parentId);
    List<Region> search(String keyword);
    Region getById(Long id);
}
