package com.example.pinchbackend.service;

import com.example.pinchbackend.dto.request.RecipeRequest;
import com.example.pinchbackend.dto.response.IngredientResponse;
import com.example.pinchbackend.dto.response.RecipeResponse;
import com.example.pinchbackend.entity.*;
import com.example.pinchbackend.entity.Origin;
import com.example.pinchbackend.entity.SourcePlatform;
import com.example.pinchbackend.exception.AccessDeniedException;
import com.example.pinchbackend.exception.RecipeNotFoundException;
import com.example.pinchbackend.repository.RecipeRepository;
import com.example.pinchbackend.repository.UserRepository;
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
        recipe.setDescription(request.getDescription());
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
                recipe.getDescription(),
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
}