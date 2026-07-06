package com.yuhyeon.devwebide.project.service;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class LocalProjectFileStorageService implements ProjectFileStorageService {

    private final Path storageRoot;

    public LocalProjectFileStorageService(
            @Value("${app.project.storage-root:./storage}") String storageRoot
    ) {
        this.storageRoot = Path.of(storageRoot);
    }

    @Override
    public StoredFile save(
            Project project,
            ProjectFile projectFile,
            String content,
            Integer versionNo
    ) {
        try {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

            Path projectRootPath = resolveProjectRootPath(project);
            Path currentFilePath = resolveCurrentFilePath(projectRootPath, projectFile);
            Path versionFilePath = resolveVersionFilePath(projectRootPath, projectFile, versionNo);

            Files.createDirectories(currentFilePath.getParent());
            Files.createDirectories(versionFilePath.getParent());

            Files.write(currentFilePath, bytes);
            Files.write(versionFilePath, bytes);

            return new StoredFile(
                    versionFilePath.toString(),
                    sha256(bytes),
                    (long) bytes.length
            );
        } catch (Exception e) {
            throw new IllegalStateException("프로젝트 파일 저장에 실패했습니다.", e);
        }
    }

    private Path resolveProjectRootPath(Project project) {
        String storagePath = removeLeadingSlash(project.getStoragePath());

        return storageRoot
                .resolve(storagePath)
                .normalize();
    }

    private Path resolveCurrentFilePath(
            Path projectRootPath,
            ProjectFile projectFile
    ) {
        String filePath = removeLeadingSlash(projectFile.getPath());

        return projectRootPath
                .resolve(filePath)
                .normalize();
    }

    private Path resolveVersionFilePath(
            Path projectRootPath,
            ProjectFile projectFile,
            Integer versionNo
    ) {
        return projectRootPath
                .resolve(".versions")
                .resolve(String.valueOf(projectFile.getId()))
                .resolve("v" + versionNo)
                .normalize();
    }

    private String removeLeadingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        if (value.startsWith("/")) {
            return value.substring(1);
        }

        return value;
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);

            StringBuilder builder = new StringBuilder();

            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 해시 생성에 실패했습니다.", e);
        }
    }
}