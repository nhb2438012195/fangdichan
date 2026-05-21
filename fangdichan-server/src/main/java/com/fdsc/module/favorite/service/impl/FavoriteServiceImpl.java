package com.fdsc.module.favorite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fdsc.common.result.PageResult;
import com.fdsc.module.favorite.entity.Favorite;
import com.fdsc.module.favorite.mapper.FavoriteMapper;
import com.fdsc.module.favorite.service.FavoriteService;
import com.fdsc.module.property.entity.Property;
import com.fdsc.module.property.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteMapper favoriteMapper;

    @Override
    public boolean toggleFavorite(Long customerId, Long propertyId) {
        Favorite existing = favoriteMapper.selectOne(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getCustomerId, customerId)
                .eq(Favorite::getPropertyId, propertyId));
        if (existing != null) {
            favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getId, existing.getId()));
            return false;
        }
        Favorite fav = new Favorite();
        fav.setCustomerId(customerId);
        fav.setPropertyId(propertyId);
        favoriteMapper.insert(fav);
        return true;
    }

    @Override
    public PageResult<Property> listByCustomer(Long customerId, int page, int size) {
        return PageResult.of(java.util.Collections.emptyList(), page, size, 0);
    }
}
