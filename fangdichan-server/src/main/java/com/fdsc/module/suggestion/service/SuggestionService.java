package com.fdsc.module.suggestion.service;

import com.fdsc.common.result.PageResult;
import com.fdsc.module.suggestion.entity.Suggestion;

import java.math.BigDecimal;

public interface SuggestionService {
    void create(Long customerId, Long companyId, String desiredType, BigDecimal desiredPriceMin, BigDecimal desiredPriceMax, String content);
    PageResult<Suggestion> listByCustomer(Long customerId, int page, int size);
    PageResult<Suggestion> listByCompany(Long companyId, int page, int size);
}
