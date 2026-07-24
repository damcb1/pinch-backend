package com.example.pinchbackend.exception;

public class RecipeNotFoundException extends RuntimeException {
    public RecipeNotFoundException(Integer id) {
        super("No se encontró la receta con id: " + id);
    }
}