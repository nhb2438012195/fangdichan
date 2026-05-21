package com.fdsc.module.suggestion.controller;

import com.fdsc.common.result.Result;
import com.fdsc.common.result.PageResult;
import com.fdsc.module.suggestion.entity.Suggestion;
import com.fdsc.module.suggestion.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent/suggestion")
@RequiredArgsConstructor
public class AgentSuggestionController {
    private final SuggestionService suggestionService;

    @GetMapping("/list")
    public Result<PageResult<Suggestion>> list(@AuthenticationPrincipal Long userId,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.success(suggestionService.listByCompany(userId, page, size));
    }
}
