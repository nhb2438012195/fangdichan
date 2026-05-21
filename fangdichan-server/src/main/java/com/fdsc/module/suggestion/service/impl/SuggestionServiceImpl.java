package com.fdsc.module.suggestion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fdsc.common.result.PageResult;
import com.fdsc.module.suggestion.entity.Suggestion;
import com.fdsc.module.suggestion.mapper.SuggestionMapper;
import com.fdsc.module.suggestion.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SuggestionServiceImpl implements SuggestionService {
    private final SuggestionMapper suggestionMapper;

    @Override
    public void create(Long customerId, Long companyId, String desiredType,
                       BigDecimal desiredPriceMin, BigDecimal desiredPriceMax, String content) {
        Suggestion s = new Suggestion();
        s.setCustomerId(customerId);
        s.setCompanyId(companyId);
        s.setDesiredType(desiredType);
        s.setDesiredPriceMin(desiredPriceMin);
        s.setDesiredPriceMax(desiredPriceMax);
        s.setContent(content);
        suggestionMapper.insert(s);
    }

    @Override
    public PageResult<Suggestion> listByCustomer(Long customerId, int page, int size) {
        Page<Suggestion> p = suggestionMapper.selectPage(new Page<>(page, size),
            new LambdaQueryWrapper<Suggestion>().eq(Suggestion::getCustomerId, customerId));
        return PageResult.of(p.getRecords(), page, size, p.getTotal());
    }

    @Override
    public PageResult<Suggestion> listByCompany(Long companyId, int page, int size) {
        Page<Suggestion> p = suggestionMapper.selectPage(new Page<>(page, size),
            new LambdaQueryWrapper<Suggestion>().eq(Suggestion::getCompanyId, companyId));
        return PageResult.of(p.getRecords(), page, size, p.getTotal());
    }
}
