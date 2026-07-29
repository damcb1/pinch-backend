package com.example.pinchbackend.mapper;

import com.example.pinchbackend.dto.request.RecipeRequest;
import com.example.pinchbackend.dto.response.IngredientResponse;
import com.example.pinchbackend.dto.response.RecipeResponse;
import com.example.pinchbackend.dto.response.RecipeSummaryResponse;
import com.example.pinchbackend.entity.Recipe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecipeMapper {

    private final IngredientMapper ingredientMapper;

    public Recipe toEntity(RecipeRequest request) {
        Recipe recipe = new Recipe();
        applyRequest(recipe, request);
        return recipe;
    }

    public void updateEntity(Recipe recipe, RecipeRequest request) {
        applyRequest(recipe, request);
    }

    private void applyRequest(Recipe recipe, RecipeRequest request) {
        recipe.setTitle(request.getTitle());
        recipe.setSourceUrl(request.getSourceUrl());
        recipe.setImageUrl(request.getImageUrl());
        recipe.setTimeMinutes(request.getTimeMinutes());
        recipe.setServings(request.getServings());
        recipe.setCuisine(request.getCuisine());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setSteps(request.getSteps());

        if (recipe.getIngredients() == null) {
            recipe.setIngredients(new ArrayList<>());
        } else {
            recipe.getIngredients().clear();
        }
        request.getIngredients().forEach(ir ->
                recipe.getIngredients().add(ingredientMapper.toEntity(ir, recipe))
        );
    }

    public RecipeResponse toResponse(Recipe recipe) {
        List<IngredientResponse> ingredients = recipe.getIngredients().stream()
                .map(ingredientMapper::toResponse)
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

    public RecipeSummaryResponse toSummary(Recipe recipe) {
        return new RecipeSummaryResponse(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getImageUrl(),
                recipe.getTimeMinutes(),
                recipe.getServings(),
                recipe.getCuisine(),
                recipe.getDifficulty(),
                recipe.getOrigin()
        );
    }
}