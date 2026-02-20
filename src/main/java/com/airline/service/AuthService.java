package com.airline.service;

import com.airline.dto.AuthDto;
import com.airline.entity.User;
import com.airline.exception.ConflictException;
import com.airline.repository.UserRepository;
import com.airline.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;
    private final JwtUtil           jwtUtil;
    private final AuthenticationManager authManager;

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
            .fullName(request.getFullName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .phone(request.getPhone())
            .role(User.Role.PASSENGER)
            .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user);
        return new AuthDto.AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        // Throws BadCredentialsException if invalid
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow();

        String token = jwtUtil.generateToken(user);
        return new AuthDto.AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }
}
