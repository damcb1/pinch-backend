package com.example.pinchbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter

public class ImportUrlRequest {
    @NotBlank(message = "Pega un enlace para importar")
    @URL(message = "El enlace no es una URL válida")
    private String url;
}