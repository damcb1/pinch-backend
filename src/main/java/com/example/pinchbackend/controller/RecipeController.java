package com.example.pinchbackend.controller;

import com.example.pinchbackend.dto.request.RecipeRequest;
import com.example.pinchbackend.dto.response.RecipeResponse;
import com.example.pinchbackend.dto.response.RecipeSummaryResponse;
import com.example.pinchbackend.entity.Origin;
import com.example.pinchbackend.security.CustomUserDetails;
import com.example.pinchbackend.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> getById(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RecipeResponse recipe = recipeService.getById(id, userDetails.getUsername());
        return ResponseEntity.ok(recipe);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody RecipeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RecipeResponse updated = recipeService.update(id, request, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        recipeService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<RecipeSummaryResponse>> getAll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) Integer maxTime,
            @RequestParam(required = false) Origin origin
            ) {
        return ResponseEntity.ok(
                recipeService.search(userDetails.getUsername(), q, cuisine, maxTime, origin)
        );
    }
}