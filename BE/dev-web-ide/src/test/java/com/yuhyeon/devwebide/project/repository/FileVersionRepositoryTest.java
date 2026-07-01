package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.FileVersion;
import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectFile;
import com.yuhyeon.devwebide.project.domain.ProjectFileStatus;
import com.yuhyeon.devwebide.project.domain.ProjectFileType;
import com.yuhyeon.devwebide.project.domain.ProjectSaveBatch;
import com.yuhyeon.devwebide.project.domain.ProjectSaveBatchStatus;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.runtime.domain.RuntimeStatus;
import com.yuhyeon.devwebide.runtime.repository.RuntimeRepository;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FileVersionRepositoryTest {

    @Autowired
    private FileVersionRepository fileVersionRepository;

    @Autowired
    private ProjectSaveBatchRepository projectSaveBatchRepository;

    @Autowired
    private ProjectFileRepository projectFileRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RuntimeRepository runtimeRepository;

    @Test
    @DisplayName("파일 버전을 저장한다")
    void saveFileVersion() {
        // given
        User user = saveUser("user1@test.com", "회원1");
        Runtime runtime = saveRuntime("nodejs-20");
        Project project = savePersonalProject(user, runtime, "파일 버전 저장 프로젝트");
        ProjectFile projectFile = saveProjectFile(project, "app.js", "/src/app.js");
        ProjectSaveBatch saveBatch = saveProjectSaveBatch(project, user, 1);

        FileVersion fileVersion = FileVersion.builder()
                .projectFile(projectFile)
                .saveBatch(saveBatch)
                .versionNo(1)
                .storagePath("/projects/" + project.getId() + "/.versions/" + projectFile.getId() + "/v1")
                .contentHash("hash-v1")
                .sizeBytes(100L)
                .build();

        // when
        FileVersion savedVersion = fileVersionRepository.save(fileVersion);

        // then
        assertThat(savedVersion.getId()).isNotNull();
        assertThat(savedVersion.getProjectFile().getId()).isEqualTo(projectFile.getId());
        assertThat(savedVersion.getSaveBatch().getId()).isEqualTo(saveBatch.getId());
        assertThat(savedVersion.getVersionNo()).isEqualTo(1);
        assertThat(savedVersion.getStoragePath()).contains("/.versions/");
        assertThat(savedVersion.getContentHash()).isEqualTo("hash-v1");
        assertThat(savedVersion.getSizeBytes()).isEqualTo(100L);
        assertThat(savedVersion.getCreatedAt()).isNotNull();
        assertThat(savedVersion.hasSaveBatch()).isTrue();
    }

    @Test
    @DisplayName("프로젝트 파일 ID 기준 버전 목록을 버전 번호 내림차순으로 조회한다")
    void findByProjectFileIdOrderByVersionNoDesc() {
        // given
        User user = saveUser("user2@test.com", "회원2");
        Runtime runtime = saveRuntime("python-3-12");
        Project project = savePersonalProject(user, runtime, "버전 목록 프로젝트");
        ProjectFile projectFile = saveProjectFile(project, "main.py", "/src/main.py");
        ProjectSaveBatch saveBatch = saveProjectSaveBatch(project, user, 2);

        FileVersion version1 = saveFileVersion(project, projectFile, saveBatch, 1, "hash-v1", 100L);
        FileVersion version2 = saveFileVersion(project, projectFile, saveBatch, 2, "hash-v2", 200L);
        FileVersion version3 = saveFileVersion(project, projectFile, saveBatch, 3, "hash-v3", 300L);

        // when
        List<FileVersion> result =
                fileVersionRepository.findByProjectFileIdOrderByVersionNoDesc(
                        projectFile.getId()
                );

        // then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(FileVersion::getId)
                .containsExactly(
                        version3.getId(),
                        version2.getId(),
                        version1.getId()
                );
    }

    @Test
    @DisplayName("프로젝트 파일 ID 기준 최신 버전을 조회한다")
    void findTopByProjectFileIdOrderByVersionNoDesc() {
        // given
        User user = saveUser("user3@test.com", "회원3");
        Runtime runtime = saveRuntime("java-21");
        Project project = savePersonalProject(user, runtime, "최신 버전 프로젝트");
        ProjectFile projectFile = saveProjectFile(project, "Main.java", "/src/Main.java");
        ProjectSaveBatch saveBatch = saveProjectSaveBatch(project, user, 2);

        saveFileVersion(project, projectFile, saveBatch, 1, "hash-v1", 100L);
        FileVersion latestVersion = saveFileVersion(project, projectFile, saveBatch, 2, "hash-v2", 200L);

        // when
        Optional<FileVersion> result =
                fileVersionRepository.findTopByProjectFileIdOrderByVersionNoDesc(
                        projectFile.getId()
                );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(latestVersion.getId());
        assertThat(result.get().getVersionNo()).isEqualTo(2);
        assertThat(result.get().getContentHash()).isEqualTo("hash-v2");
    }

    @Test
    @DisplayName("프로젝트 파일 ID와 버전 번호 기준 파일 버전을 조회한다")
    void findByProjectFileIdAndVersionNo() {
        // given
        User user = saveUser("user4@test.com", "회원4");
        Runtime runtime = saveRuntime("cpp-17");
        Project project = savePersonalProject(user, runtime, "특정 버전 조회 프로젝트");
        ProjectFile projectFile = saveProjectFile(project, "main.cpp", "/src/main.cpp");
        ProjectSaveBatch saveBatch = saveProjectSaveBatch(project, user, 1);

        FileVersion fileVersion = saveFileVersion(
                project,
                projectFile,
                saveBatch,
                1,
                "hash-cpp-v1",
                150L
        );

        // when
        Optional<FileVersion> result =
                fileVersionRepository.findByProjectFileIdAndVersionNo(
                        projectFile.getId(),
                        1
                );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(fileVersion.getId());
        assertThat(result.get().getVersionNo()).isEqualTo(1);
        assertThat(result.get().getContentHash()).isEqualTo("hash-cpp-v1");
    }

    @Test
    @DisplayName("프로젝트 파일 ID와 버전 번호 기준 파일 버전 존재 여부를 확인한다")
    void existsByProjectFileIdAndVersionNo() {
        // given
        User user = saveUser("user5@test.com", "회원5");
        Runtime runtime = saveRuntime("nodejs-18");
        Project project = savePersonalProject(user, runtime, "버전 존재 확인 프로젝트");
        ProjectFile projectFile = saveProjectFile(project, "index.js", "/src/index.js");
        ProjectSaveBatch saveBatch = saveProjectSaveBatch(project, user, 1);

        saveFileVersion(project, projectFile, saveBatch, 1, "hash-index-v1", 100L);

        // when
        boolean existsVersion1 =
                fileVersionRepository.existsByProjectFileIdAndVersionNo(
                        projectFile.getId(),
                        1
                );

        boolean existsVersion2 =
                fileVersionRepository.existsByProjectFileIdAndVersionNo(
                        projectFile.getId(),
                        2
                );

        // then
        assertThat(existsVersion1).isTrue();
        assertThat(existsVersion2).isFalse();
    }

    @Test
    @DisplayName("저장 배치 ID 기준 파일 버전 목록을 조회한다")
    void findBySaveBatchIdOrderByCreatedAtAsc() {
        // given
        User user = saveUser("user6@test.com", "회원6");
        Runtime runtime = saveRuntime("python-3-11");
        Project project = savePersonalProject(user, runtime, "저장 배치 버전 조회 프로젝트");

        ProjectFile firstFile = saveProjectFile(project, "app.py", "/src/app.py");
        ProjectFile secondFile = saveProjectFile(project, "utils.py", "/src/utils.py");
        ProjectSaveBatch saveBatch = saveProjectSaveBatch(project, user, 2);

        FileVersion firstVersion = saveFileVersion(
                project,
                firstFile,
                saveBatch,
                1,
                "hash-app-v1",
                100L
        );

        FileVersion secondVersion = saveFileVersion(
                project,
                secondFile,
                saveBatch,
                1,
                "hash-utils-v1",
                80L
        );

        // when
        List<FileVersion> result =
                fileVersionRepository.findBySaveBatchIdOrderByCreatedAtAsc(
                        saveBatch.getId()
                );

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(FileVersion::getId)
                .contains(firstVersion.getId(), secondVersion.getId());
    }

    @Test
    @DisplayName("저장 배치 ID 기준 파일 버전 개수를 조회한다")
    void countBySaveBatchId() {
        // given
        User user = saveUser("user7@test.com", "회원7");
        Runtime runtime = saveRuntime("java-17");
        Project project = savePersonalProject(user, runtime, "저장 배치 개수 프로젝트");

        ProjectFile firstFile = saveProjectFile(project, "Main.java", "/src/Main.java");
        ProjectFile secondFile = saveProjectFile(project, "User.java", "/src/User.java");
        ProjectSaveBatch saveBatch = saveProjectSaveBatch(project, user, 2);

        saveFileVersion(project, firstFile, saveBatch, 1, "hash-main-v1", 100L);
        saveFileVersion(project, secondFile, saveBatch, 1, "hash-user-v1", 120L);

        // when
        long count = fileVersionRepository.countBySaveBatchId(saveBatch.getId());

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("프로젝트 파일 ID 기준 파일 버전 개수를 조회한다")
    void countByProjectFileId() {
        // given
        User user = saveUser("user8@test.com", "회원8");
        Runtime runtime = saveRuntime("nodejs-22");
        Project project = savePersonalProject(user, runtime, "파일 버전 개수 프로젝트");
        ProjectFile projectFile = saveProjectFile(project, "server.js", "/src/server.js");
        ProjectSaveBatch saveBatch = saveProjectSaveBatch(project, user, 3);

        saveFileVersion(project, projectFile, saveBatch, 1, "hash-server-v1", 100L);
        saveFileVersion(project, projectFile, saveBatch, 2, "hash-server-v2", 110L);
        saveFileVersion(project, projectFile, saveBatch, 3, "hash-server-v3", 120L);

        // when
        long count = fileVersionRepository.countByProjectFileId(projectFile.getId());

        // then
        assertThat(count).isEqualTo(3);
    }

    private User saveUser(String email, String nickname) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    private Runtime saveRuntime(String name) {
        Runtime runtime = Runtime.builder()
                .name(name)
                .displayName(name)
                .version("1.0.0")
                .dockerImage("test/" + name)
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.ACTIVE)
                .build();

        return runtimeRepository.save(runtime);
    }

    private Project savePersonalProject(
            User ownerUser,
            Runtime runtime,
            String name
    ) {
        Project project = Project.builder()
                .ownerUser(ownerUser)
                .guestSession(null)
                .runtime(runtime)
                .name(name)
                .description("테스트 프로젝트입니다.")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/test/" + name)
                .build();

        return projectRepository.save(project);
    }

    private ProjectFile saveProjectFile(
            Project project,
            String name,
            String path
    ) {
        ProjectFile projectFile = ProjectFile.builder()
                .project(project)
                .parentFile(null)
                .name(name)
                .path(path)
                .fileType(ProjectFileType.FILE)
                .mimeType("text/plain")
                .sizeBytes(0L)
                .status(ProjectFileStatus.ACTIVE)
                .build();

        return projectFileRepository.save(projectFile);
    }

    private ProjectSaveBatch saveProjectSaveBatch(
            Project project,
            User user,
            Integer savedFileCount
    ) {
        ProjectSaveBatch saveBatch = ProjectSaveBatch.builder()
                .project(project)
                .user(user)
                .guestSession(null)
                .status(ProjectSaveBatchStatus.SUCCESS)
                .savedFileCount(savedFileCount)
                .build();

        return projectSaveBatchRepository.save(saveBatch);
    }

    private FileVersion saveFileVersion(
            Project project,
            ProjectFile projectFile,
            ProjectSaveBatch saveBatch,
            Integer versionNo,
            String contentHash,
            Long sizeBytes
    ) {
        FileVersion fileVersion = FileVersion.builder()
                .projectFile(projectFile)
                .saveBatch(saveBatch)
                .versionNo(versionNo)
                .storagePath(
                        "/projects/"
                                + project.getId()
                                + "/.versions/"
                                + projectFile.getId()
                                + "/v"
                                + versionNo
                )
                .contentHash(contentHash)
                .sizeBytes(sizeBytes)
                .build();

        return fileVersionRepository.save(fileVersion);
    }
}