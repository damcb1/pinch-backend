package com.example.pinchbackend.dto.response;

import com.example.pinchbackend.entity.Difficulty;
import com.example.pinchbackend.entity.Origin;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecipeSummaryResponse {
    private Integer id;
    private String title;
    private String imageUrl;
    private Integer timeMinutes;
    private Integer servings;
    private String cuisine;
    private Difficulty difficulty;
    private Origin origin;
}