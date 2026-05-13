package com.library.LibraryService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.library.LibraryCustomExceptions.InvalidOperationException;
import com.library.LibraryDTO.AuthResponse;
import com.library.LibraryDTO.LoginRequest;
import com.library.LibraryDTO.RegisterRequest;
import com.library.LibraryEntity.LibraryMmember.AppUser;
import com.library.LibraryRepository.MemberRepo.AppUserRepository;
import com.library.LibrarySecurity.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AppUserRepository appUserRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "expirationMs", 3600000L);
    }

    @Test
    void register_shouldCreateUserWithDefaultRole_whenRoleIsNull() {
        RegisterRequest req = new RegisterRequest("alice", "Password1", null);
        when(appUserRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("ENC");
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("tok");

        AuthResponse resp = authService.register(req);

        assertEquals("tok", resp.getToken());
        assertEquals("alice", resp.getUsername());
        assertEquals("USER", resp.getRole());
        assertEquals(3600000L, resp.getExpiresInMs());
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void register_shouldUseProvidedRole_whenSpecified() {
        RegisterRequest req = new RegisterRequest("bob", "Password1", "admin");
        when(appUserRepository.existsByUsername("bob")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("ENC");
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("tok");

        AuthResponse resp = authService.register(req);

        assertEquals("ADMIN", resp.getRole());
    }

    @Test
    void register_shouldUseDefaultRole_whenRoleIsBlank() {
        RegisterRequest req = new RegisterRequest("carol", "Password1", "   ");
        when(appUserRepository.existsByUsername("carol")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("ENC");
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("tok");

        AuthResponse resp = authService.register(req);

        assertEquals("USER", resp.getRole());
    }

    @Test
    void register_shouldThrow_whenUsernameAlreadyExists() {
        RegisterRequest req = new RegisterRequest("dup", "Password1", null);
        when(appUserRepository.existsByUsername("dup")).thenReturn(true);

        assertThrows(InvalidOperationException.class, () -> authService.register(req));
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnTokenForValidCredentials() {
        LoginRequest req = new LoginRequest("alice", "Password1");
        AppUser user = new AppUser("alice", "ENC", "USER");
        when(appUserRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("tok");

        AuthResponse resp = authService.login(req);

        assertEquals("tok", resp.getToken());
        assertEquals("alice", resp.getUsername());
        assertEquals("USER", resp.getRole());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_shouldThrow_whenAuthenticationFails() {
        LoginRequest req = new LoginRequest("alice", "wrong");
        doThrow(new BadCredentialsException("bad")).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(BadCredentialsException.class, () -> authService.login(req));
        verify(appUserRepository, never()).findByUsername(any());
    }

    @Test
    void login_shouldThrow_whenUserMissingFromRepository() {
        LoginRequest req = new LoginRequest("ghost", "Password1");
        when(appUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(InvalidOperationException.class, () -> authService.login(req));
    }
}// change 1 and change 