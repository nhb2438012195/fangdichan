package com.fdsc.module.property.controller;

import com.fdsc.common.result.Result;
import com.fdsc.common.result.PageResult;
import com.fdsc.module.property.entity.Property;
import com.fdsc.module.property.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/customer/property")
@RequiredArgsConstructor
public class CustomerPropertyController {
    private final PropertyService propertyService;

    @GetMapping("/search")
    public Result<PageResult<Property>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) BigDecimal priceMin,
            @RequestParam(required = false) BigDecimal priceMax,
            @RequestParam(required = false) BigDecimal areaMin,
            @RequestParam(required = false) BigDecimal areaMax,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(propertyService.search(keyword, district, roomType, priceMin, priceMax, areaMin, areaMax, page, size));
    }

    @GetMapping("/{id}")
    public Result<Property> detail(@PathVariable Long id) {
        return Result.success(propertyService.getDetail(id));
    }
}
