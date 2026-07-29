package com.example.pinchbackend.service;

import com.example.pinchbackend.dto.request.RecipeRequest;
import com.example.pinchbackend.dto.response.RecipeResponse;
import com.example.pinchbackend.dto.response.RecipeSummaryResponse;
import com.example.pinchbackend.entity.*;
import com.example.pinchbackend.exception.AccessDeniedException;
import com.example.pinchbackend.exception.RecipeNotFoundException;
import com.example.pinchbackend.mapper.RecipeMapper;
import com.example.pinchbackend.repository.RecipeRepository;
import com.example.pinchbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final RecipeMapper recipeMapper;

    public RecipeResponse create(RecipeRequest request, String userEmail) {
        return save(request, userEmail, Origin.MANUAL, SourcePlatform.MANUAL);
    }

    public RecipeResponse createImported(RecipeRequest request, String userEmail) {
        return save(request, userEmail, Origin.IMPORTED, SourcePlatform.WEB);
    }

    private RecipeResponse save(RecipeRequest request, String userEmail, Origin origin, SourcePlatform sourcePlatform) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + userEmail));

        Recipe recipe = recipeMapper.toEntity(request);
        recipe.setOrigin(origin);
        recipe.setSourcePlatform(sourcePlatform);
        recipe.setAuthor(author);

        Recipe saved = recipeRepository.save(recipe);
        return recipeMapper.toResponse(saved);
    }

    public RecipeResponse getById(Integer id, String userEmail) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        if (!recipe.getAuthor().getEmail().equals(userEmail)) {
            throw new AccessDeniedException();
        }

        return recipeMapper.toResponse(recipe);
    }

    @Transactional
    public RecipeResponse update(Integer id, RecipeRequest request, String userEmail) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        if (!recipe.getAuthor().getEmail().equals(userEmail)) {
            throw new AccessDeniedException();
        }

        String oldImageUrl = recipe.getImageUrl();

        recipeMapper.updateEntity(recipe, request);

        Recipe saved = recipeRepository.save(recipe);
        if (oldImageUrl != null && !oldImageUrl.equals(request.getImageUrl())) {
            fileUploadService.deleteByUrl(oldImageUrl);
        }
        return recipeMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Integer id, String userEmail) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        if (!recipe.getAuthor().getEmail().equals(userEmail)) {
            throw new AccessDeniedException();
        }

        recipeRepository.delete(recipe);
        fileUploadService.deleteByUrl(recipe.getImageUrl());
    }

    public List<RecipeSummaryResponse> search(String userEmail, String q, String cuisine, Integer minTime, Integer maxTime, Origin origin, Difficulty difficulty) {
        String query = (q == null) ? null : q.trim().toLowerCase();

        return recipeRepository.findByAuthorEmailOrderByCreatedAtDesc(userEmail).stream()
                .filter(r -> query == null || query.isEmpty() || matchesQuery(r, query))
                .filter(r -> cuisine == null || cuisine.equalsIgnoreCase(r.getCuisine()))
                .filter(r -> minTime == null || (r.getTimeMinutes() != null && r.getTimeMinutes() >= minTime))
                .filter(r -> maxTime == null || (r.getTimeMinutes() != null && r.getTimeMinutes() <= maxTime))
                .filter(r -> origin == null || origin.equals(r.getOrigin()))
                .filter(r -> difficulty == null || difficulty.equals(r.getDifficulty()))
                .map(recipeMapper::toSummary)
                .toList();
    }

    private boolean matchesQuery(Recipe r, String query) {
        if (r.getTitle() != null && r.getTitle().toLowerCase().contains(query)) {
            return true;
        }

        return r.getIngredients() != null && r.getIngredients().stream()
                .anyMatch(i -> i.getName() != null && i.getName().toLowerCase().contains(query));
    }
}