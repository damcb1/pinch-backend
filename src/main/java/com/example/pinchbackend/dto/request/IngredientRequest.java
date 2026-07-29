package com.example.pinchbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class IngredientRequest {
    private String qty;

    private String unit;

    @NotBlank(message = "El nombre del ingrediente es obligatorio")
    private String name;
}