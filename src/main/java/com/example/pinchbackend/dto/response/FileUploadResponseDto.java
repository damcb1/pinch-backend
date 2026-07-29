package com.example.pinchbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class FileUploadResponseDto {
    private String fileName;
    private String contentType;
    private long size;
    private String url;
}