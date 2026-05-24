package com.Abhishek.authify.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDocumentResponse {
    private UUID id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Timestamp uploadedAt;
}
