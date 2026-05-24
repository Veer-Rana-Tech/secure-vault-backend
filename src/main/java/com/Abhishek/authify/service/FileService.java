package com.Abhishek.authify.service;

import com.Abhishek.authify.entity.FileDocument;
import com.Abhishek.authify.entity.UserEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public interface FileService {
    FileDocument uploadFile(MultipartFile file, UserEntity user) throws IOException;
    List<FileDocument> getUserFiles(UserEntity user);
    FileDocument getFileById(UUID id, UserEntity user);
    Path getFilePath(UUID id, UserEntity user);
    void deleteFile(UUID id, UserEntity user) throws IOException;
}
