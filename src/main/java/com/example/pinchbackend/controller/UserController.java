package com.example.pinchbackend.controller;

import com.example.pinchbackend.dto.response.UserResponse;
import com.example.pinchbackend.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.pinchbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String email = userDetails.getUser().getEmail();
        return ResponseEntity.ok(new UserResponse(email));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.deleteAccount(userDetails.getUser().getEmail());
        return ResponseEntity.noContent().build();
    }
}