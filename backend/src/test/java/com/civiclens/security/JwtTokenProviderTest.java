package com.civiclens.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider Tests")
@SuppressWarnings("null")
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private String testSecret = "test-secret-key-that-is-very-long-for-testing-purposes-only";
    private long testExpiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", testExpiration);
    }

    @Test
    @DisplayName("Should generate valid token with userId and email")
    void testGenerateTokenSuccess() {
        String userId = "user123";
        String email = "test@example.com";

        String token = jwtTokenProvider.generateToken(userId, email);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should extract userId from valid token")
    void testGetUserIdFromTokenSuccess() {
        String userId = "user123";
        String email = "test@example.com";
        String token = jwtTokenProvider.generateToken(userId, email);

        String extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("Should return null for invalid token userId extraction")
    void testGetUserIdFromInvalidToken() {
        String invalidToken = "invalid.token.here";

        String result = jwtTokenProvider.getUserIdFromToken(invalidToken);

        assertNull(result);
    }

    @Test
    @DisplayName("Should extract email from valid token")
    void testGetEmailFromTokenSuccess() {
        String userId = "user123";
        String email = "test@example.com";
        String token = jwtTokenProvider.generateToken(userId, email);

        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Should return null for invalid token email extraction")
    void testGetEmailFromInvalidToken() {
        String invalidToken = "invalid.token.here";

        String result = jwtTokenProvider.getEmailFromToken(invalidToken);

        assertNull(result);
    }

    @Test
    @DisplayName("Should validate valid token successfully")
    void testValidateTokenSuccess() {
        String userId = "user123";
        String email = "test@example.com";
        String token = jwtTokenProvider.generateToken(userId, email);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject invalid token")
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject null token")
    void testValidateNullToken() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject empty token")
    void testValidateEmptyToken() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should handle token with malformed signature")
    void testValidateMalformedSignature() {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMTIzIn0.invalid";

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void testGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtTokenProvider.generateToken("user1", "user1@example.com");
        String token2 = jwtTokenProvider.generateToken("user2", "user2@example.com");

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should maintain token consistency across multiple validations")
    void testTokenConsistency() {
        String userId = "user123";
        String email = "test@example.com";
        String token = jwtTokenProvider.generateToken(userId, email);

        // Validate multiple times
        assertTrue(jwtTokenProvider.validateToken(token));
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
    }
}
