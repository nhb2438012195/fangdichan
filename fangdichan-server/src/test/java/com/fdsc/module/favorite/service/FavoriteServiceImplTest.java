package com.fdsc.module.favorite.service;

import com.fdsc.module.favorite.entity.Favorite;
import com.fdsc.module.favorite.mapper.FavoriteMapper;
import com.fdsc.module.favorite.service.impl.FavoriteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {
    @Mock private FavoriteMapper favoriteMapper;
    private FavoriteServiceImpl favoriteService;

    @BeforeEach
    void setUp() { favoriteService = new FavoriteServiceImpl(favoriteMapper); }

    @Test
    void toggleFavorite_shouldAddWhenNotExists() {
        when(favoriteMapper.selectOne(any())).thenReturn(null);
        boolean result = favoriteService.toggleFavorite(1L, 1L);
        assertTrue(result);
        verify(favoriteMapper).insert(any());
    }

    @Test
    void toggleFavorite_shouldRemoveWhenExists() {
        when(favoriteMapper.selectOne(any())).thenReturn(new Favorite());
        boolean result = favoriteService.toggleFavorite(1L, 1L);
        assertFalse(result);
        verify(favoriteMapper).delete(any());
    }
}
