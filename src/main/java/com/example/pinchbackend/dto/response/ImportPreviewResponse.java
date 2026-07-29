package com.example.pinchbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor

public class ImportPreviewResponse {
    private String title;
    private String sourceUrl;
    private String imageUrl;
    private Integer timeMinutes;
    private Integer servings;
    private String cuisine;

    private List<IngredientResponse> ingredients;

    private List<String> steps;
}
