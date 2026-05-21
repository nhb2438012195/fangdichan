package com.fdsc.module.suggestion.service;

import com.fdsc.module.suggestion.mapper.SuggestionMapper;
import com.fdsc.module.suggestion.service.impl.SuggestionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceImplTest {
    @Mock private SuggestionMapper suggestionMapper;
    private SuggestionServiceImpl suggestionService;

    @BeforeEach
    void setUp() { suggestionService = new SuggestionServiceImpl(suggestionMapper); }

    @Test
    void create_shouldInsert() {
        suggestionService.create(1L, 1L, "三室", null, null, "求购");
        verify(suggestionMapper).insert(any());
    }

    @Test
    void listByCustomer_shouldReturnPage() {
        when(suggestionMapper.selectPage(any(), any())).thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>());
        suggestionService.listByCustomer(1L, 1, 10);
        verify(suggestionMapper).selectPage(any(), any());
    }
}
