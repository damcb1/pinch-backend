package com.example.pinchbackend.controller;

import com.example.pinchbackend.dto.request.RecipeRequest;
import com.example.pinchbackend.dto.response.RecipeResponse;
import com.example.pinchbackend.security.CustomUserDetails;
import com.example.pinchbackend.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor

public class RecipeController {
    private final RecipeService recipeService;

    @PostMapping
    public ResponseEntity<RecipeResponse> create(
            @Valid @RequestBody RecipeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RecipeResponse created = recipeService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}