package com.example.pinchbackend.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException() {
        super("No tienes permiso para acceder a esta receta");
    }
}