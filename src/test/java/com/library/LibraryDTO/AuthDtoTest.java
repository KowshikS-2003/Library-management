package com.library.LibraryDTO;

import com.library.LibraryEntity.LibraryMmember.AppUser;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuthDtoTest {

    // ---- AuthResponse ----

    @Test
    void authResponse_defaultConstructor() {
        AuthResponse r = new AuthResponse();
        assertNull(r.getToken());
        assertNull(r.getUsername());
        assertNull(r.getRole());
        assertEquals(0L, r.getExpiresInMs());
    }

    @Test
    void authResponse_parameterizedConstructorAndGetters() {
        AuthResponse r = new AuthResponse("tok", "alice", "USER", 1000L);
        assertEquals("tok", r.getToken());
        assertEquals("alice", r.getUsername());
        assertEquals("USER", r.getRole());
        assertEquals(1000L, r.getExpiresInMs());
    }

    @Test
    void authResponse_setters() {
        AuthResponse r = new AuthResponse();
        r.setToken("t");
        r.setUsername("u");
        r.setRole("ADMIN");
        r.setExpiresInMs(42L);
        assertEquals("t", r.getToken());
        assertEquals("u", r.getUsername());
        assertEquals("ADMIN", r.getRole());
        assertEquals(42L, r.getExpiresInMs());
    }

    // ---- LoginRequest ----

    @Test
    void loginRequest_defaultConstructor() {
        LoginRequest r = new LoginRequest();
        assertNull(r.getUsername());
        assertNull(r.getPassword());
    }

    @Test
    void loginRequest_parameterizedConstructorAndSetters() {
        LoginRequest r = new LoginRequest("alice", "pw");
        assertEquals("alice", r.getUsername());
        assertEquals("pw", r.getPassword());
        r.setUsername("bob");
        r.setPassword("pw2");
        assertEquals("bob", r.getUsername());
        assertEquals("pw2", r.getPassword());
    }

    // ---- RegisterRequest ----

    @Test
    void registerRequest_defaultConstructor() {
        RegisterRequest r = new RegisterRequest();
        assertNull(r.getUsername());
        assertNull(r.getPassword());
        assertNull(r.getRole());
    }

    @Test
    void registerRequest_parameterizedConstructorAndSetters() {
        RegisterRequest r = new RegisterRequest("alice", "Password1", "USER");
        assertEquals("alice", r.getUsername());
        assertEquals("Password1", r.getPassword());
        assertEquals("USER", r.getRole());
        r.setUsername("bob");
        r.setPassword("Password2");
        r.setRole("ADMIN");
        assertEquals("bob", r.getUsername());
        assertEquals("Password2", r.getPassword());
        assertEquals("ADMIN", r.getRole());
    }

    // ---- AppUser entity ----

    @Test
    void appUser_defaultConstructor() {
        AppUser u = new AppUser();
        assertNull(u.getId());
        assertNull(u.getUsername());
        assertNull(u.getPassword());
        assertNull(u.getRole());
        // createdAt is initialized at field declaration
        assertNotNull(u.getCreatedAt());
    }

    @Test
    void appUser_parameterizedConstructor_setsCreatedAt() {
        AppUser u = new AppUser("alice", "pwd", "USER");
        assertEquals("alice", u.getUsername());
        assertEquals("pwd", u.getPassword());
        assertEquals("USER", u.getRole());
        assertNotNull(u.getCreatedAt());
    }

    @Test
    void appUser_setters() {
        AppUser u = new AppUser();
        LocalDateTime now = LocalDateTime.now();
        u.setId(5L);
        u.setUsername("alice");
        u.setPassword("pwd");
        u.setRole("ADMIN");
        u.setCreatedAt(now);
        assertEquals(5L, u.getId());
        assertEquals("alice", u.getUsername());
        assertEquals("pwd", u.getPassword());
        assertEquals("ADMIN", u.getRole());
        assertEquals(now, u.getCreatedAt());
    }
}
