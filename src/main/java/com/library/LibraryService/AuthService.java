package com.library.LibraryService;

import com.library.LibraryCustomExceptions.InvalidOperationException;
import com.library.LibraryDTO.AuthResponse;
import com.library.LibraryDTO.LoginRequest;
import com.library.LibraryDTO.RegisterRequest;
import com.library.LibraryEntity.LibraryMmember.AppUser;
import com.library.LibraryRepository.MemberRepo.AppUserRepository;
import com.library.LibrarySecurity.JwtService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String DEFAULT_ROLE = "USER";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    public AuthService(AppUserRepository appUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.getUsername())) {
            logger.warn("Registration attempted for existing username: {}", request.getUsername());
            throw new InvalidOperationException("Username already taken");
        }

        String role = (request.getRole() == null || request.getRole().isBlank())
                ? DEFAULT_ROLE
                : request.getRole().toUpperCase();

        AppUser user = new AppUser(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                role);
        appUserRepository.save(user);
        logger.info("Registered new user: {}", user.getUsername());

        String token = jwtService.generateToken(toUserDetails(user));
        return new AuthResponse(token, user.getUsername(), user.getRole(), expirationMs);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidOperationException("Invalid credentials"));

        String token = jwtService.generateToken(toUserDetails(user));
        logger.info("User logged in: {}", user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getRole(), expirationMs);
    }

    private static User toUserDetails(AppUser user) {
        return new User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(() -> "ROLE_" + user.getRole()));
    }
}
