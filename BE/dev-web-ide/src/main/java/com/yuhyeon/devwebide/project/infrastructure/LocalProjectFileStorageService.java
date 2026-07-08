package com.yuhyeon.devwebide.project.infrastructure;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectFile;
import com.yuhyeon.devwebide.project.service.ProjectFileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.stream.Stream;

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

    @Override
    public void createFile(
            Project project,
            ProjectFile projectFile
    ) {
        validateCreateRequest(project, projectFile);

        try {
            Path projectRootPath = resolveProjectRootPath(project);
            Path currentFilePath = resolveCurrentFilePath(projectRootPath, projectFile);

            Files.createDirectories(currentFilePath.getParent());
            Files.write(currentFilePath, new byte[0]);
        } catch (Exception e) {
            throw new IllegalStateException("프로젝트 파일 생성에 실패했습니다.", e);
        }
    }

    @Override
    public void createDirectory(
            Project project,
            ProjectFile projectFile
    ) {
        validateCreateRequest(project, projectFile);

        try {
            Path projectRootPath = resolveProjectRootPath(project);
            Path currentDirectoryPath = resolveCurrentFilePath(projectRootPath, projectFile);

            Files.createDirectories(currentDirectoryPath);
        } catch (Exception e) {
            throw new IllegalStateException("프로젝트 디렉터리 생성에 실패했습니다.", e);
        }
    }

    @Override
    public void rename(
            Project project,
            ProjectFile projectFile,
            String newPath
    ) {
        validateRenameRequest(project, projectFile, newPath);

        try {
            Path projectRootPath = resolveProjectRootPath(project);
            Path currentPath = resolveCurrentFilePath(projectRootPath, projectFile);
            Path newFilePath = resolvePath(projectRootPath, newPath);

            Files.createDirectories(newFilePath.getParent());
            Files.move(currentPath, newFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new IllegalStateException("프로젝트 파일 이름 변경에 실패했습니다.", e);
        }
    }

    @Override
    public void delete(
            Project project,
            ProjectFile projectFile
    ) {
        validateDeleteRequest(project, projectFile);

        try {
            Path projectRootPath = resolveProjectRootPath(project);
            Path currentPath = resolveCurrentFilePath(projectRootPath, projectFile);

            if (!Files.exists(currentPath)) {
                throw new IllegalStateException("삭제할 프로젝트 파일을 찾을 수 없습니다.");
            }

            if (Files.isDirectory(currentPath)) {
                deleteDirectoryRecursively(currentPath);
            } else {
                Files.delete(currentPath);
            }
        } catch (Exception e) {
            throw new IllegalStateException("프로젝트 파일 삭제에 실패했습니다.", e);
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

    private void validateCreateRequest(
            Project project,
            ProjectFile projectFile
    ) {
        if (project == null) {
            throw new IllegalArgumentException("?袁⑥쨮??븍뱜???袁⑸땾??낅빍??");
        }

        if (projectFile == null) {
            throw new IllegalArgumentException("?袁⑥쨮??븍뱜 ???뵬?? ?袁⑸땾??낅빍??");
        }
    }

    private void validateRenameRequest(
            Project project,
            ProjectFile projectFile,
            String newPath
    ) {
        if (project == null) {
            throw new IllegalArgumentException("?袁⑥쨮??븍뱜???袁⑸땾??낅빍??");
        }

        if (projectFile == null) {
            throw new IllegalArgumentException("?袁⑥쨮??븍뱜 ???뵬?? ?袁⑸땾??낅빍??");
        }

        if (newPath == null || newPath.isBlank()) {
            throw new IllegalArgumentException("변경할 파일 경로는 필수입니다.");
        }
    }

    private void validateDeleteRequest(
            Project project,
            ProjectFile projectFile
    ) {
        if (project == null) {
            throw new IllegalArgumentException("?熬곣뫁夷??釉띾콦???熬곣뫖????낅퉵??");
        }

        if (projectFile == null) {
            throw new IllegalArgumentException("?熬곣뫁夷??釉띾콦 ???逾?? ?熬곣뫖????낅퉵??");
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

    private Path resolvePath(
            Path projectRootPath,
            String path
    ) {
        String filePath = removeLeadingSlash(path);

        Path resolvedPath = projectRootPath
                .resolve(filePath)
                .normalize();

        validatePathInsideBase(projectRootPath, resolvedPath);

        return resolvedPath;
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

    private void deleteDirectoryRecursively(Path directoryPath) throws Exception {
        try (Stream<Path> paths = Files.walk(directoryPath)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            throw new IllegalStateException(
                                    "프로젝트 디렉터리 삭제에 실패했습니다.",
                                    e
                            );
                        }
                    });
        }
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
