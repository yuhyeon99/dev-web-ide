package com.yuhyeon.devwebide.project.infrastructure;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectFile;
import com.yuhyeon.devwebide.project.service.ProjectFileStorageService;
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
            @Value("${app.storage.project-root:./storage}") String storageRoot
    ) {
        this.storageRoot = Path.of(storageRoot)
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public StoredFile save(
            Project project,
            ProjectFile projectFile,
            String content,
            Integer versionNo
    ) {
        validateSaveRequest(project, projectFile, content, versionNo);

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

    @Override
    public String read(
            Project project,
            ProjectFile projectFile
    ) {
        validateReadRequest(project, projectFile);

        try {
            Path projectRootPath = resolveProjectRootPath(project);
            Path currentFilePath = resolveCurrentFilePath(projectRootPath, projectFile);

            return Files.readString(currentFilePath, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("프로젝트 파일 읽기에 실패했습니다.", e);
        }
    }

    private void validateSaveRequest(
            Project project,
            ProjectFile projectFile,
            String content,
            Integer versionNo
    ) {
        if (project == null) {
            throw new IllegalArgumentException("프로젝트는 필수입니다.");
        }

        if (projectFile == null) {
            throw new IllegalArgumentException("프로젝트 파일은 필수입니다.");
        }

        if (content == null) {
            throw new IllegalArgumentException("파일 내용은 필수입니다.");
        }

        if (versionNo == null || versionNo <= 0) {
            throw new IllegalArgumentException("파일 버전 번호는 1 이상이어야 합니다.");
        }
    }

    private void validateReadRequest(
            Project project,
            ProjectFile projectFile
    ) {
        if (project == null) {
            throw new IllegalArgumentException("?꾨줈?앺듃???꾩닔?낅땲??");
        }

        if (projectFile == null) {
            throw new IllegalArgumentException("?꾨줈?앺듃 ?뚯씪? ?꾩닔?낅땲??");
        }
    }

    private Path resolveProjectRootPath(Project project) {
        String storagePath = removeLeadingSlash(project.getStoragePath());

        Path projectRootPath = storageRoot
                .resolve(storagePath)
                .normalize();

        validatePathInsideBase(storageRoot, projectRootPath);

        return projectRootPath;
    }

    private Path resolveCurrentFilePath(
            Path projectRootPath,
            ProjectFile projectFile
    ) {
        String filePath = removeLeadingSlash(projectFile.getPath());

        Path currentFilePath = projectRootPath
                .resolve(filePath)
                .normalize();

        validatePathInsideBase(projectRootPath, currentFilePath);

        return currentFilePath;
    }

    private Path resolveVersionFilePath(
            Path projectRootPath,
            ProjectFile projectFile,
            Integer versionNo
    ) {
        Path versionFilePath = projectRootPath
                .resolve(".versions")
                .resolve(String.valueOf(projectFile.getId()))
                .resolve("v" + versionNo)
                .normalize();

        validatePathInsideBase(projectRootPath, versionFilePath);

        return versionFilePath;
    }

    private void validatePathInsideBase(Path basePath, Path targetPath) {
        if (!targetPath.startsWith(basePath)) {
            throw new IllegalArgumentException("허용되지 않은 파일 경로입니다.");
        }
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
