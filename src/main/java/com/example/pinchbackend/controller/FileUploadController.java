package com.example.pinchbackend.controller;

import com.example.pinchbackend.dto.response.FileUploadResponseDto;
import com.example.pinchbackend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDto> upload(@RequestParam("file") MultipartFile file) {
        FileUploadResponseDto response = fileUploadService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}