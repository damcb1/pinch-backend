package com.example.pinchbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class IngredientResponse {
    private String qty;
    private String unit;
    private String name;
}