package com.example.pinchbackend.service;

import com.example.pinchbackend.dto.request.LoginRequest;
import com.example.pinchbackend.dto.request.RegisterRequest;
import com.example.pinchbackend.dto.response.LoginResponse;
import com.example.pinchbackend.entity.User;
import com.example.pinchbackend.entity.Role;
import com.example.pinchbackend.exception.EmailAlreadyExistsException;
import com.example.pinchbackend.exception.InvalidCredentialsException;
import com.example.pinchbackend.repository.UserRepository;
import com.example.pinchbackend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }

        String token = jwtUtil.generateToken(request.getEmail());
        return new LoginResponse(token, request.getEmail());
    }
}