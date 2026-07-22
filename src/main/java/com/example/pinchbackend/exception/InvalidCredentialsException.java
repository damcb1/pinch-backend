package com.example.pinchbackend.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Email o contraseña incorrectos");
    }
}