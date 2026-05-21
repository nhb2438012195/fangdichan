package com.fdsc.module.favorite.controller;

import com.fdsc.common.result.Result;
import com.fdsc.module.favorite.service.FavoriteService;
import com.fdsc.module.property.entity.Property;
import com.fdsc.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/favorite")
@RequiredArgsConstructor
public class CustomerFavoriteController {
    private final FavoriteService favoriteService;

    @PostMapping("/{propertyId}")
    public Result<Boolean> toggle(@AuthenticationPrincipal Long userId, @PathVariable Long propertyId) {
        return Result.success(favoriteService.toggleFavorite(userId, propertyId));
    }

    @GetMapping("/list")
    public Result<PageResult<Property>> list(@AuthenticationPrincipal Long userId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.success(favoriteService.listByCustomer(userId, page, size));
    }
}
