package com.example.pinchbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ImportTextRequest {
    @NotBlank(message = "Pega el texto de la receta")

    @Size(min = 50, message = "El texto es demasiado corto para ser una receta")
    private String text;
}