package com.yuhyeon.devwebide.project.infrastructure;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectFile;
import com.yuhyeon.devwebide.project.service.ProjectFileStorageService.StoredFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class LocalProjectFileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("파일을 현재 경로와 버전 경로에 저장한다")
    void saveFile() throws Exception {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        given(project.getStoragePath()).willReturn("/projects/1");

        ProjectFile projectFile = mock(ProjectFile.class);
        given(projectFile.getId()).willReturn(10L);
        given(projectFile.getPath()).willReturn("/src/App.js");

        String content = "console.log('hello');";

        // when
        StoredFile storedFile =
                storageService.save(project, projectFile, content, 1);

        // then
        Path currentFilePath = tempDir
                .resolve("projects/1/src/App.js")
                .normalize();

        Path versionFilePath = tempDir
                .resolve("projects/1/.versions/10/v1")
                .normalize();

        assertThat(Files.exists(currentFilePath)).isTrue();
        assertThat(Files.exists(versionFilePath)).isTrue();

        assertThat(Files.readString(currentFilePath)).isEqualTo(content);
        assertThat(Files.readString(versionFilePath)).isEqualTo(content);

        assertThat(storedFile.storagePath())
                .isEqualTo(versionFilePath.toString());

        assertThat(storedFile.sizeBytes())
                .isEqualTo((long) content.getBytes(StandardCharsets.UTF_8).length);

        assertThat(storedFile.contentHash())
                .isEqualTo(sha256(content.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("없는 디렉토리는 자동으로 생성한다")
    void createDirectoriesAutomatically() {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        given(project.getStoragePath()).willReturn("/projects/1");

        ProjectFile projectFile = mock(ProjectFile.class);
        given(projectFile.getId()).willReturn(20L);
        given(projectFile.getPath()).willReturn("/src/main/java/App.java");

        // when
        storageService.save(project, projectFile, "class App {}", 1);

        // then
        Path savedFilePath = tempDir
                .resolve("projects/1/src/main/java/App.java")
                .normalize();

        assertThat(Files.exists(savedFilePath)).isTrue();
    }

    @Test
    @DisplayName("파일 저장 시 기존 파일을 덮어쓴다")
    void overwriteExistingFile() throws Exception {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        given(project.getStoragePath()).willReturn("/projects/1");

        ProjectFile projectFile = mock(ProjectFile.class);
        given(projectFile.getId()).willReturn(30L);
        given(projectFile.getPath()).willReturn("/README.md");

        storageService.save(project, projectFile, "before", 1);

        // when
        storageService.save(project, projectFile, "after", 2);

        // then
        Path currentFilePath = tempDir
                .resolve("projects/1/README.md")
                .normalize();

        Path version1Path = tempDir
                .resolve("projects/1/.versions/30/v1")
                .normalize();

        Path version2Path = tempDir
                .resolve("projects/1/.versions/30/v2")
                .normalize();

        assertThat(Files.readString(currentFilePath)).isEqualTo("after");
        assertThat(Files.readString(version1Path)).isEqualTo("before");
        assertThat(Files.readString(version2Path)).isEqualTo("after");
    }

    @Test
    @DisplayName("저장된 현재 파일 내용을 읽는다")
    void readSavedFile() {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        given(project.getStoragePath()).willReturn("/projects/1");

        ProjectFile projectFile = mock(ProjectFile.class);
        given(projectFile.getId()).willReturn(35L);
        given(projectFile.getPath()).willReturn("/src/App.js");

        storageService.save(project, projectFile, "console.log('hello');", 1);

        // when
        String content = storageService.read(project, projectFile);

        // then
        assertThat(content).isEqualTo("console.log('hello');");
    }

    @Test
    @DisplayName("존재하지 않는 파일을 읽으면 예외가 발생한다")
    void throwExceptionWhenReadMissingFile() {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        given(project.getStoragePath()).willReturn("/projects/1");

        ProjectFile projectFile = mock(ProjectFile.class);
        given(projectFile.getPath()).willReturn("/src/Missing.js");

        // when & then
        assertThatThrownBy(() -> storageService.read(project, projectFile))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("프로젝트 파일 읽기에 실패했습니다.");
    }

    @Test
    @DisplayName("파일 읽기 시 경로 탈출 시도가 있으면 예외가 발생한다")
    void rejectInvalidProjectFilePathWhenRead() {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        given(project.getStoragePath()).willReturn("/projects/1");

        ProjectFile projectFile = mock(ProjectFile.class);
        given(projectFile.getPath()).willReturn("/../../outside.txt");

        // when & then
        assertThatThrownBy(() -> storageService.read(project, projectFile))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("프로젝트 파일 읽기에 실패했습니다.");
    }

    @Test
    @DisplayName("빈 파일을 생성한다")
    void createFile() {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        given(project.getStoragePath()).willReturn("/projects/1");

        ProjectFile projectFile = mock(ProjectFile.class);
        given(projectFile.getPath()).willReturn("/src/App.js");

        // when
        storageService.createFile(project, projectFile);

        // then
        Path currentFilePath = tempDir
                .resolve("projects/1/src/App.js")
                .normalize();

        assertThat(Files.exists(currentFilePath)).isTrue();
        assertThat(currentFilePath).isRegularFile();
    }

    @Test
    @DisplayName("디렉터리를 생성한다")
    void createDirectory() {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        given(project.getStoragePath()).willReturn("/projects/1");

        ProjectFile projectFile = mock(ProjectFile.class);
        given(projectFile.getPath()).willReturn("/src/components");

        // when
        storageService.createDirectory(project, projectFile);

        // then
        Path directoryPath = tempDir
                .resolve("projects/1/src/components")
                .normalize();

        assertThat(Files.exists(directoryPath)).isTrue();
        assertThat(directoryPath).isDirectory();
    }

    @Test
    @DisplayName("중첩 경로의 부모 디렉터리를 자동 생성한다")
    void createParentDirectoriesAutomatically() {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        given(project.getStoragePath()).willReturn("/projects/1");

        ProjectFile projectFile = mock(ProjectFile.class);
        given(projectFile.getPath()).willReturn("/src/main/App.java");

        // when
        storageService.createFile(project, projectFile);

        // then
        Path filePath = tempDir
                .resolve("projects/1/src/main/App.java")
                .normalize();

        assertThat(Files.exists(filePath)).isTrue();
    }

    @Test
    @DisplayName("파일 생성 시 경로 탈출 시도가 있으면 예외가 발생한다")
    void rejectInvalidProjectFilePathWhenCreateFile() {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        given(project.getStoragePath()).willReturn("/projects/1");

        ProjectFile projectFile = mock(ProjectFile.class);
        given(projectFile.getPath()).willReturn("/../../outside.txt");

        // when & then
        assertThatThrownBy(() -> storageService.createFile(project, projectFile))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("프로젝트 파일 생성에 실패했습니다.");
    }

    @Test
    @DisplayName("프로젝트 경로가 저장 루트 밖으로 벗어나면 예외가 발생한다")
    void rejectInvalidProjectStoragePath() {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        given(project.getStoragePath()).willReturn("../../outside");

        ProjectFile projectFile = mock(ProjectFile.class);
        given(projectFile.getId()).willReturn(40L);
        given(projectFile.getPath()).willReturn("/src/App.js");

        // when & then
        assertThatThrownBy(() ->
                storageService.save(project, projectFile, "content", 1)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessage("프로젝트 파일 저장에 실패했습니다.");
    }

    @Test
    @DisplayName("파일 경로가 프로젝트 루트 밖으로 벗어나면 예외가 발생한다")
    void rejectInvalidProjectFilePath() {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        given(project.getStoragePath()).willReturn("/projects/1");

        ProjectFile projectFile = mock(ProjectFile.class);
        given(projectFile.getId()).willReturn(50L);
        given(projectFile.getPath()).willReturn("/../../outside.txt");

        // when & then
        assertThatThrownBy(() ->
                storageService.save(project, projectFile, "content", 1)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessage("프로젝트 파일 저장에 실패했습니다.");
    }

    @Test
    @DisplayName("versionNo가 null이면 예외가 발생한다")
    void rejectNullVersionNo() {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        ProjectFile projectFile = mock(ProjectFile.class);

        // when & then
        assertThatThrownBy(() ->
                storageService.save(project, projectFile, "content", null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 버전 번호는 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("versionNo가 0 이하이면 예외가 발생한다")
    void rejectInvalidVersionNo() {
        // given
        LocalProjectFileStorageService storageService =
                new LocalProjectFileStorageService(tempDir.toString());

        Project project = mock(Project.class);
        ProjectFile projectFile = mock(ProjectFile.class);

        // when & then
        assertThatThrownBy(() ->
                storageService.save(project, projectFile, "content", 0)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 버전 번호는 1 이상이어야 합니다.");
    }

    private String sha256(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);

        StringBuilder builder = new StringBuilder();

        for (byte b : hash) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }
}
