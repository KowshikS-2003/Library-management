package com.library.LibrarySecurity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails user;

    private static final String SECRET =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);
        user = new User("alice", "pwd", Collections.emptyList());
    }

    @Test
    void generateToken_shouldProduceTokenWithExpectedSubject() {
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("alice", jwtService.extractUsername(token));
    }

    @Test
    void generateToken_withExtraClaims_shouldIncludeThem() {
        Map<String, Object> extras = new HashMap<>();
        extras.put("role", "ADMIN");
        String token = jwtService.generateToken(extras, user);

        String role = jwtService.extractClaim(token, c -> c.get("role", String.class));
        assertEquals("ADMIN", role);
    }

    @Test
    void isTokenValid_shouldReturnTrue_forFreshTokenAndMatchingUser() {
        String token = jwtService.generateToken(user);
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenUsernameMismatches() {
        String token = jwtService.generateToken(user);
        UserDetails other = new User("bob", "pwd", Collections.emptyList());
        assertFalse(jwtService.isTokenValid(token, other));
    }

    @Test
    void isTokenValid_shouldThrow_whenTokenExpired() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String expired = jwtService.generateToken(user);
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(expired, user));
    }

    @Test
    void extractUsername_shouldThrow_whenSignatureInvalid() {
        String token = jwtService.generateToken(user);
        // Tamper with last character to break signature
        String tampered = token.substring(0, token.length() - 2)
                + (token.endsWith("A") ? "B" : "A");
        assertThrows(SignatureException.class, () -> jwtService.extractUsername(tampered));
    }

    @Test
    void extractClaim_shouldExposeExpirationDate() {
        String token = jwtService.generateToken(user);
        assertNotNull(jwtService.extractClaim(token, Claims::getExpiration));
    }
}
