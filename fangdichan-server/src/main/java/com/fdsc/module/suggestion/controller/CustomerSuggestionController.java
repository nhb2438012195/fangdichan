package com.fdsc.module.suggestion.controller;

import com.fdsc.common.result.Result;
import com.fdsc.common.result.PageResult;
import com.fdsc.module.suggestion.entity.Suggestion;
import com.fdsc.module.suggestion.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/customer/suggestion")
@RequiredArgsConstructor
public class CustomerSuggestionController {
    private final SuggestionService suggestionService;

    @PostMapping
    public Result<?> create(@AuthenticationPrincipal Long userId,
                             @RequestParam Long companyId,
                             @RequestParam(required = false) String desiredType,
                             @RequestParam(required = false) BigDecimal desiredPriceMin,
                             @RequestParam(required = false) BigDecimal desiredPriceMax,
                             @RequestParam String content) {
        suggestionService.create(userId, companyId, desiredType, desiredPriceMin, desiredPriceMax, content);
        return Result.success(null);
    }

    @GetMapping("/list")
    public Result<PageResult<Suggestion>> list(@AuthenticationPrincipal Long userId,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.success(suggestionService.listByCustomer(userId, page, size));
    }
}
