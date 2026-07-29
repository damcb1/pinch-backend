package com.example.pinchbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class ErrorResponse {
    private int status;
    private String message;
    private String field;
}