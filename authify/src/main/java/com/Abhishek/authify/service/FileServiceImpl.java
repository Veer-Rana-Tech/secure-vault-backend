package com.Abhishek.authify.service;

import com.Abhishek.authify.entity.FileDocument;
import com.Abhishek.authify.entity.UserEntity;
import com.Abhishek.authify.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_TYPES = {
        "application/pdf",
        "image/jpeg",
        "image/png",
        "image/webp",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };

    @Override
    public FileDocument uploadFile(MultipartFile file, UserEntity user) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        String fileType = file.getContentType();
        if (!isAllowedFileType(fileType)) {
            throw new IllegalArgumentException("File type not allowed");
        }

        // Create user directory
        Path userDir = Paths.get(uploadDir, user.getUserId());
        Files.createDirectories(userDir);

        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID() + "_" + originalFileName;

        Path filePath = userDir.resolve(uniqueFileName);

        // Save file
        Files.write(filePath, file.getBytes());

        // Save metadata to database
        FileDocument fileDocument = FileDocument.builder()
                .fileName(originalFileName)
                .fileType(fileType)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .user(user)
                .build();

        return fileRepository.save(fileDocument);
    }

    @Override
    public List<FileDocument> getUserFiles(UserEntity user) {
        return fileRepository.findByUser(user);
    }

    @Override
    public FileDocument getFileById(UUID id, UserEntity user) {
        return fileRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NoSuchElementException("File not found or access denied"));
    }

    @Override
    public Path getFilePath(UUID id, UserEntity user) {
        FileDocument file = getFileById(id, user);
        Path path = Paths.get(file.getFilePath());

        if (!Files.exists(path)) {
            throw new NoSuchElementException("File not found on disk");
        }

        return path;
    }

    @Override
    public void deleteFile(UUID id, UserEntity user) throws IOException {
        FileDocument file = getFileById(id, user);
        Path path = Paths.get(file.getFilePath());

        // Delete file from disk
        if (Files.exists(path)) {
            Files.delete(path);
        }

        // Delete metadata from database
        fileRepository.delete(file);
        log.info("File deleted: {}", file.getFileName());
    }

    private boolean isAllowedFileType(String contentType) {
        if (contentType == null) {
            return false;
        }

        for (String allowedType : ALLOWED_TYPES) {
            if (contentType.equalsIgnoreCase(allowedType)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
}
