package com.Abhishek.authify.controller;

import com.Abhishek.authify.entity.FileDocument;
import com.Abhishek.authify.entity.UserEntity;
import com.Abhishek.authify.io.FileDocumentResponse;
import com.Abhishek.authify.repository.UserRepostory;
import com.Abhishek.authify.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;
    private final UserRepostory userRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @CurrentSecurityContext(expression = "authentication?.name") String email) {
        try {
            // Get user by email
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

            // Upload file
            FileDocument uploadedFile = fileService.uploadFile(file, user);

            // Convert to response DTO
            FileDocumentResponse response = FileDocumentResponse.builder()
                    .id(uploadedFile.getId())
                    .fileName(uploadedFile.getFileName())
                    .fileType(uploadedFile.getFileType())
                    .fileSize(uploadedFile.getFileSize())
                    .uploadedAt(uploadedFile.getUploadedAt())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (IOException e) {
            log.error("Error uploading file", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Failed to upload file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            log.error("Unexpected error during file upload", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getUserFiles(
            @CurrentSecurityContext(expression = "authentication?.name") String email) {
        try {
            // Get user by email
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

            // Get user files
            List<FileDocument> files = fileService.getUserFiles(user);

            // Convert to response DTOs
            List<FileDocumentResponse> responses = files.stream()
                    .map(file -> FileDocumentResponse.builder()
                            .id(file.getId())
                            .fileName(file.getFileName())
                            .fileType(file.getFileType())
                            .fileSize(file.getFileSize())
                            .uploadedAt(file.getUploadedAt())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error fetching user files", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Failed to fetch files");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> downloadFile(
            @PathVariable UUID id,
            @CurrentSecurityContext(expression = "authentication?.name") String email) {
        try {
            // Get user by email
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

            // Get file metadata
            FileDocument fileDocument = fileService.getFileById(id, user);

            // Get file path
            Path file = fileService.getFilePath(id, user);

            Resource resource = new UrlResource(file.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileDocument.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileDocument.getFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("Error creating URL resource", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Error accessing file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error downloading file", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Failed to download file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(
            @PathVariable UUID id,
            @CurrentSecurityContext(expression = "authentication?.name") String email) {
        try {
            // Get user by email
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

            // Delete file
            fileService.deleteFile(id, user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "File deleted successfully");
            return ResponseEntity.ok(response);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting file", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", "Failed to delete file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
