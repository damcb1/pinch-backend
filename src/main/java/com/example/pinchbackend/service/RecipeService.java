package com.example.pinchbackend.service;

import com.example.pinchbackend.dto.request.RecipeRequest;
import com.example.pinchbackend.dto.response.IngredientResponse;
import com.example.pinchbackend.dto.response.RecipeResponse;
import com.example.pinchbackend.dto.response.RecipeSummaryResponse;
import com.example.pinchbackend.entity.*;
import com.example.pinchbackend.exception.AccessDeniedException;
import com.example.pinchbackend.exception.RecipeNotFoundException;
import com.example.pinchbackend.repository.RecipeRepository;
import com.example.pinchbackend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    public RecipeResponse create(RecipeRequest request, String userEmail) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + userEmail));

        Recipe recipe = new Recipe();
        recipe.setTitle(request.getTitle());
        recipe.setSourceUrl(request.getSourceUrl());
        recipe.setImageUrl(request.getImageUrl());
        recipe.setTimeMinutes(request.getTimeMinutes());
        recipe.setServings(request.getServings());
        recipe.setCuisine(request.getCuisine());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setSteps(request.getSteps());
        recipe.setOrigin(Origin.MANUAL);
        recipe.setSourcePlatform(SourcePlatform.MANUAL);
        recipe.setAuthor(author);

        List<Ingredient> ingredients = request.getIngredients().stream().map(ir -> {
            Ingredient ing = new Ingredient();
            ing.setQty(ir.getQty());
            ing.setUnit(ir.getUnit());
            ing.setName(ir.getName());
            ing.setRecipe(recipe);
            return ing;
        }).toList();
        recipe.setIngredients(ingredients);

        Recipe saved = recipeRepository.save(recipe);
        return toResponse(saved);
    }

    private RecipeResponse toResponse(Recipe recipe) {
        List<IngredientResponse> ingredients = recipe.getIngredients().stream()
                .map(i -> new IngredientResponse(i.getQty(), i.getUnit(), i.getName()))
                .toList();

        return new RecipeResponse(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getSourceUrl(),
                recipe.getImageUrl(),
                recipe.getTimeMinutes(),
                recipe.getServings(),
                recipe.getCuisine(),
                recipe.getDifficulty(),
                recipe.getOrigin(),
                ingredients,
                recipe.getSteps()
        );
    }

    public RecipeResponse getById(Integer id, String userEmail) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        if (!recipe.getAuthor().getEmail().equals(userEmail)) {
            throw new AccessDeniedException();
        }

        return toResponse(recipe);
    }

    @Transactional
    public RecipeResponse update(Integer id, RecipeRequest request, String userEmail) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        if (!recipe.getAuthor().getEmail().equals(userEmail)) {
            throw new AccessDeniedException();
        }

        recipe.setTitle(request.getTitle());
        recipe.setSourceUrl(request.getSourceUrl());
        recipe.setImageUrl(request.getImageUrl());
        recipe.setTimeMinutes(request.getTimeMinutes());
        recipe.setServings(request.getServings());
        recipe.setCuisine(request.getCuisine());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setSteps(request.getSteps());

        recipe.getIngredients().clear();
        request.getIngredients().forEach(ir -> {
            Ingredient ing = new Ingredient();
            ing.setQty(ir.getQty());
            ing.setUnit(ir.getUnit());
            ing.setName(ir.getName());
            ing.setRecipe(recipe);
            recipe.getIngredients().add(ing);
        });

        Recipe saved = recipeRepository.save(recipe);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Integer id, String userEmail) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        if (!recipe.getAuthor().getEmail().equals(userEmail)) {
            throw new AccessDeniedException();
        }

        recipeRepository.delete(recipe);
    }

    private RecipeSummaryResponse toSummary(Recipe r) {
        return new RecipeSummaryResponse(
                r.getId(),
                r.getTitle(),
                r.getImageUrl(),
                r.getTimeMinutes(),
                r.getServings(),
                r.getCuisine(),
                r.getDifficulty(),
                r.getOrigin()
        );
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
                .map(this::toSummary)
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