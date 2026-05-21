package com.fdsc.module.favorite.service;

import com.fdsc.common.result.PageResult;
import com.fdsc.module.property.entity.Property;

public interface FavoriteService {
    boolean toggleFavorite(Long customerId, Long propertyId);
    PageResult<Property> listByCustomer(Long customerId, int page, int size);
}
