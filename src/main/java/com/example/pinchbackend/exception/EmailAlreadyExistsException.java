package com.example.pinchbackend.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Este correo ya está en uso: " + email);
    }
}