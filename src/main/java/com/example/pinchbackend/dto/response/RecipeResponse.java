package com.example.pinchbackend.dto.response;

import com.example.pinchbackend.entity.Difficulty;
import com.example.pinchbackend.entity.Origin;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor

public class RecipeResponse {
    private Integer id;
    private String title;
    private String description;
    private String sourceUrl;
    private String imageUrl;
    private Integer timeMinutes;
    private Integer servings;
    private String cuisine;
    private Difficulty difficulty;
    private Origin origin;
    private List<IngredientResponse> ingredients;
    private List<String> steps;
}