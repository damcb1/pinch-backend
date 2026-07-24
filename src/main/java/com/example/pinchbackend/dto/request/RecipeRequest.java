package com.example.pinchbackend.dto.request;

import com.example.pinchbackend.entity.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class RecipeRequest {
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede superar los 150 caracteres")
    private String title;

    private String description;

    private String sourceUrl;

    private String imageUrl;

    private Integer timeMinutes;

    private Integer servings;

    private String cuisine;

    private Difficulty difficulty;

    @NotEmpty(message = "Añade al menos un ingrediente")
    @Valid
    private List<IngredientRequest> ingredients;

    @NotEmpty(message = "Añade al menos un paso")
    private List<String> steps;
}