package com.example.pinchbackend.mapper;

import com.example.pinchbackend.dto.request.IngredientRequest;
import com.example.pinchbackend.dto.response.IngredientResponse;
import com.example.pinchbackend.entity.Ingredient;
import com.example.pinchbackend.entity.Recipe;
import org.springframework.stereotype.Component;

@Component
public class IngredientMapper {

    public Ingredient toEntity(IngredientRequest request, Recipe recipe) {
        Ingredient ingredient = new Ingredient();
        ingredient.setQty(request.getQty());
        ingredient.setUnit(request.getUnit());
        ingredient.setName(request.getName());
        ingredient.setRecipe(recipe);
        return ingredient;
    }

    public IngredientResponse toResponse(Ingredient ingredient) {
        return new IngredientResponse(
                ingredient.getQty(),
                ingredient.getUnit(),
                ingredient.getName()
        );
    }
}