package com.trpg.service;

import com.trpg.dto.auth.LoginRequest;
import com.trpg.dto.auth.LoginResponse;
import com.trpg.dto.auth.RegisterRequest;
import com.trpg.model.User;
import com.trpg.model.UserRole;
import com.trpg.repository.UserRepository;
import com.trpg.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Register a new user account.
     *
     * @param request registration details (username, email, password)
     * @return the persisted {@link User} entity
     * @throws IllegalArgumentException if username or email is already taken
     */
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email '" + request.getEmail() + "' is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new user: id={}, username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    /**
     * Authenticate a user and issue a JWT token.
     *
     * @param request login credentials (username, password)
     * @return {@link LoginResponse} containing the JWT, username, and role
     * @throws IllegalArgumentException if credentials are invalid
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        log.info("User '{}' logged in successfully", user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }
}
