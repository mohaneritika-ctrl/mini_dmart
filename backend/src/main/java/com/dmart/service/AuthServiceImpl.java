package com.dmart.service;

import com.dmart.dto.request.LoginRequest;
import com.dmart.dto.request.RegisterRequest;
import com.dmart.dto.response.AuthResponse;
import com.dmart.entity.Role;
import com.dmart.entity.User;
import com.dmart.exception.DuplicateEmailException;
import com.dmart.repository.UserRepository;
import com.dmart.security.CustomUserDetailsService;
import com.dmart.security.JwtService;
import com.dmart.entity.AuditLog;
import com.dmart.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("Email is already registered: " + normalizedEmail);
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .role(Role.CUSTOMER)
                .active(true)
                .lastLoginAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        // Audit registration
        try {
            auditLogRepository.save(
                    AuditLog.builder()
                            .user(savedUser)
                            .action("USER_REGISTER")
                            .entityType("USER")
                            .entityId(savedUser.getId())
                            .description("New customer registered: " + savedUser.getEmail())
                            .build()
            );
        } catch (Exception ignored) {}

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String jwtToken = jwtService.generateToken(userDetails, savedUser.getId(), savedUser.getRole());

        return AuthResponse.builder()
                .token(jwtToken)
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole())
                .lastLoginAt(savedUser.getLastLoginAt())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getActive() != null && !user.getActive()) {
            throw new BadCredentialsException("User account is inactive");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Audit log login event
        try {
            auditLogRepository.save(
                    AuditLog.builder()
                            .user(user)
                            .action("USER_LOGIN")
                            .entityType("USER")
                            .entityId(user.getId())
                            .description(String.format("User %s (%s) signed in successfully.", user.getName(), user.getRole()))
                            .build()
            );
        } catch (Exception ignored) {}

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtService.generateToken(userDetails, user.getId(), user.getRole());

        return AuthResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}