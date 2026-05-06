package com.library.LibraryController;

import com.library.LibraryCustomExceptions.InvalidOperationException;
import com.library.LibraryDTO.AuthResponse;
import com.library.LibraryDTO.LoginRequest;
import com.library.LibraryDTO.RegisterRequest;
import com.library.LibraryService.AuthService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    @InjectMocks private AuthController authController;

    @Test
    void register_shouldReturnOkWithBody() {
        RegisterRequest req = new RegisterRequest("alice", "Password1", null);
        AuthResponse expected = new AuthResponse("tok", "alice", "USER", 3600000L);
        when(authService.register(any(RegisterRequest.class))).thenReturn(expected);

        ResponseEntity<AuthResponse> resp = authController.register(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(expected, resp.getBody());
        verify(authService).register(req);
    }

    @Test
    void register_shouldPropagateException_whenServiceFails() {
        RegisterRequest req = new RegisterRequest("dup", "Password1", null);
        when(authService.register(any())).thenThrow(new InvalidOperationException("Username already taken"));

        assertThrows(InvalidOperationException.class, () -> authController.register(req));
    }

    @Test
    void login_shouldReturnOkWithBody() {
        LoginRequest req = new LoginRequest("alice", "Password1");
        AuthResponse expected = new AuthResponse("tok", "alice", "USER", 3600000L);
        when(authService.login(any(LoginRequest.class))).thenReturn(expected);

        ResponseEntity<AuthResponse> resp = authController.login(req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("tok", resp.getBody().getToken());
        verify(authService).login(req);
    }

    @Test
    void login_shouldPropagateException_whenCredentialsInvalid() {
        LoginRequest req = new LoginRequest("alice", "wrong");
        when(authService.login(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class, () -> authController.login(req));
    }
}
