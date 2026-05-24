package com.Abhishek.authify.repository;

import com.Abhishek.authify.entity.FileDocument;
import com.Abhishek.authify.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileDocument, UUID> {
    List<FileDocument> findByUser(UserEntity user);
    Optional<FileDocument> findByIdAndUser(UUID id, UserEntity user);
}
