package com.example.pinchbackend.service;

import com.example.pinchbackend.dto.response.FileUploadResponseDto;
import com.example.pinchbackend.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp", "gif");
    private static final List<String> ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

    @Value("${app.upload.path}")
    private String uploadPath;

    @Value("${app.upload.base-url}")
    private String baseUrl;

    public FileUploadResponseDto upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("El archivo está vacío o no se ha enviado.");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename());

        String extension = "";
        int dot = originalFilename.lastIndexOf(".");
        if (dot != -1) {
            extension = originalFilename.substring(dot + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException(
                    "Extensión '." + extension + "' no permitida. Solo se aceptan imágenes: " + ALLOWED_EXTENSIONS);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException("El archivo no es una imagen válida.");
        }

        try {
            String filename = UUID.randomUUID() + "_" + originalFilename;

            Path directory = Paths.get(uploadPath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            Path destination = directory.resolve(filename);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            String url = baseUrl + "/uploads/" + filename;

            return new FileUploadResponseDto(filename, contentType, file.getSize(), url);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
    }
}