package com.fdsc.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider("test-secret-key-for-unit-testing-purposes-only!", 3600000);
    }

    @Test
    void generateAndValidateToken_shouldSucceed() {
        String token = jwtTokenProvider.generateToken(1L, "ADMIN");
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectId() {
        String token = jwtTokenProvider.generateToken(42L, "CUSTOMER");
        assertEquals(42L, jwtTokenProvider.getUserIdFromToken(token));
    }

    @Test
    void getRoleFromToken_shouldReturnCorrectRole() {
        String token = jwtTokenProvider.generateToken(1L, "AGENT");
        assertEquals("AGENT", jwtTokenProvider.getRoleFromToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid-token"));
    }
}
