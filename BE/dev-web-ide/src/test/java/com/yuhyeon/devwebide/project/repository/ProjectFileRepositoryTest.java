package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectFile;
import com.yuhyeon.devwebide.project.domain.ProjectFileStatus;
import com.yuhyeon.devwebide.project.domain.ProjectFileType;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProjectFileRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RuntimeRepository runtimeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectFileRepository projectFileRepository;

    @Test
    @DisplayName("프로젝트 루트 디렉토리 저장")
    void saveRootDirectory() {
        Project project = createProject();

        ProjectFile rootDirectory =
                ProjectFile.createRootDirectory(project);

        ProjectFile savedRootDirectory =
                projectFileRepository.save(rootDirectory);

        assertThat(savedRootDirectory.getId()).isNotNull();
        assertThat(savedRootDirectory.getProject().getId())
                .isEqualTo(project.getId());
        assertThat(savedRootDirectory.getParentFile()).isNull();
        assertThat(savedRootDirectory.getName()).isEqualTo("/");
        assertThat(savedRootDirectory.getPath()).isEqualTo("/");
        assertThat(savedRootDirectory.getFileType())
                .isEqualTo(ProjectFileType.DIRECTORY);
        assertThat(savedRootDirectory.getMimeType()).isNull();
        assertThat(savedRootDirectory.getSizeBytes()).isEqualTo(0L);
        assertThat(savedRootDirectory.getStatus())
                .isEqualTo(ProjectFileStatus.ACTIVE);
        assertThat(savedRootDirectory.isRootDirectory()).isTrue();
    }

    @Test
    @DisplayName("프로젝트 ID와 경로로 파일 조회")
    void findByProjectIdAndPath() {
        Project project = createProject();
        ProjectFile rootDirectory = createRootDirectory(project);

        ProjectFile packageJson = createFile(
                project,
                rootDirectory,
                "package.json",
                "/package.json",
                "application/json",
                120L,
                ProjectFileStatus.ACTIVE
        );

        projectFileRepository.save(packageJson);

        Optional<ProjectFile> result =
                projectFileRepository.findByProjectIdAndPath(
                        project.getId(),
                        "/package.json"
                );

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("package.json");
        assertThat(result.get().getPath()).isEqualTo("/package.json");
        assertThat(result.get().isFile()).isTrue();
    }

    @Test
    @DisplayName("프로젝트 ID, 경로, 상태로 활성 파일 조회")
    void findByProjectIdAndPathAndStatus() {
        Project project = createProject();
        ProjectFile rootDirectory = createRootDirectory(project);

        ProjectFile appFile = createFile(
                project,
                rootDirectory,
                "App.jsx",
                "/App.jsx",
                "text/javascript",
                300L,
                ProjectFileStatus.ACTIVE
        );

        projectFileRepository.save(appFile);

        Optional<ProjectFile> result =
                projectFileRepository.findByProjectIdAndPathAndStatus(
                        project.getId(),
                        "/App.jsx",
                        ProjectFileStatus.ACTIVE
                );

        assertThat(result).isPresent();
        assertThat(result.get().getPath()).isEqualTo("/App.jsx");
        assertThat(result.get().getStatus())
                .isEqualTo(ProjectFileStatus.ACTIVE);
    }

    @Test
    @DisplayName("삭제 상태 파일은 ACTIVE 상태 조회에서 조회되지 않음")
    void findByProjectIdAndPathAndStatusDeletedFile() {
        Project project = createProject();
        ProjectFile rootDirectory = createRootDirectory(project);

        ProjectFile deletedFile = createFile(
                project,
                rootDirectory,
                "old.txt",
                "/old.txt",
                "text/plain",
                50L,
                ProjectFileStatus.DELETED
        );

        projectFileRepository.save(deletedFile);

        Optional<ProjectFile> result =
                projectFileRepository.findByProjectIdAndPathAndStatus(
                        project.getId(),
                        "/old.txt",
                        ProjectFileStatus.ACTIVE
                );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("프로젝트 ID와 경로로 파일 존재 여부 확인")
    void existsByProjectIdAndPath() {
        Project project = createProject();
        ProjectFile rootDirectory = createRootDirectory(project);

        ProjectFile readme = createFile(
                project,
                rootDirectory,
                "README.md",
                "/README.md",
                "text/markdown",
                80L,
                ProjectFileStatus.ACTIVE
        );

        projectFileRepository.save(readme);

        boolean exists =
                projectFileRepository.existsByProjectIdAndPath(
                        project.getId(),
                        "/README.md"
                );

        boolean notExists =
                projectFileRepository.existsByProjectIdAndPath(
                        project.getId(),
                        "/none.md"
                );

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("프로젝트 파일 트리 전체 조회")
    void findByProjectIdAndStatusOrderByPathAsc() {
        Project project = createProject();
        ProjectFile rootDirectory = createRootDirectory(project);

        ProjectFile srcDirectory = createDirectory(
                project,
                rootDirectory,
                "src",
                "/src",
                ProjectFileStatus.ACTIVE
        );

        ProjectFile appFile = createFile(
                project,
                srcDirectory,
                "App.jsx",
                "/src/App.jsx",
                "text/javascript",
                300L,
                ProjectFileStatus.ACTIVE
        );

        ProjectFile packageJson = createFile(
                project,
                rootDirectory,
                "package.json",
                "/package.json",
                "application/json",
                120L,
                ProjectFileStatus.ACTIVE
        );

        projectFileRepository.save(srcDirectory);
        projectFileRepository.save(appFile);
        projectFileRepository.save(packageJson);

        List<ProjectFile> result =
                projectFileRepository.findByProjectIdAndStatusOrderByPathAsc(
                        project.getId(),
                        ProjectFileStatus.ACTIVE
                );

        assertThat(result).hasSize(4);
        assertThat(result)
                .extracting(ProjectFile::getPath)
                .containsExactly(
                        "/",
                        "/package.json",
                        "/src",
                        "/src/App.jsx"
                );
    }

    @Test
    @DisplayName("특정 부모 폴더의 하위 파일/폴더 목록 조회")
    void findByProjectIdAndParentFileIdAndStatusOrderByNameAsc() {
        Project project = createProject();
        ProjectFile rootDirectory = createRootDirectory(project);

        ProjectFile srcDirectory = createDirectory(
                project,
                rootDirectory,
                "src",
                "/src",
                ProjectFileStatus.ACTIVE
        );

        ProjectFile packageJson = createFile(
                project,
                rootDirectory,
                "package.json",
                "/package.json",
                "application/json",
                120L,
                ProjectFileStatus.ACTIVE
        );

        ProjectFile appFile = createFile(
                project,
                srcDirectory,
                "App.jsx",
                "/src/App.jsx",
                "text/javascript",
                300L,
                ProjectFileStatus.ACTIVE
        );

        projectFileRepository.save(srcDirectory);
        projectFileRepository.save(packageJson);
        projectFileRepository.save(appFile);

        List<ProjectFile> result =
                projectFileRepository
                        .findByProjectIdAndParentFileIdAndStatusOrderByNameAsc(
                                project.getId(),
                                rootDirectory.getId(),
                                ProjectFileStatus.ACTIVE
                        );

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ProjectFile::getName)
                .containsExactly("package.json", "src");
    }

    @Test
    @DisplayName("프로젝트 루트 디렉토리 조회")
    void findByProjectIdAndParentFileIsNullAndStatus() {
        Project project = createProject();
        ProjectFile rootDirectory = createRootDirectory(project);

        Optional<ProjectFile> result =
                projectFileRepository.findByProjectIdAndParentFileIsNullAndStatus(
                        project.getId(),
                        ProjectFileStatus.ACTIVE
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(rootDirectory.getId());
        assertThat(result.get().isRootDirectory()).isTrue();
    }

    @Test
    @DisplayName("프로젝트 내 파일 타입 목록 조회")
    void findByProjectIdAndFileTypeAndStatusOrderByPathAsc() {
        Project project = createProject();
        ProjectFile rootDirectory = createRootDirectory(project);

        ProjectFile srcDirectory = createDirectory(
                project,
                rootDirectory,
                "src",
                "/src",
                ProjectFileStatus.ACTIVE
        );

        ProjectFile appFile = createFile(
                project,
                srcDirectory,
                "App.jsx",
                "/src/App.jsx",
                "text/javascript",
                300L,
                ProjectFileStatus.ACTIVE
        );

        ProjectFile packageJson = createFile(
                project,
                rootDirectory,
                "package.json",
                "/package.json",
                "application/json",
                120L,
                ProjectFileStatus.ACTIVE
        );

        projectFileRepository.save(srcDirectory);
        projectFileRepository.save(appFile);
        projectFileRepository.save(packageJson);

        List<ProjectFile> result =
                projectFileRepository
                        .findByProjectIdAndFileTypeAndStatusOrderByPathAsc(
                                project.getId(),
                                ProjectFileType.FILE,
                                ProjectFileStatus.ACTIVE
                        );

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ProjectFile::getPath)
                .containsExactly("/package.json", "/src/App.jsx");
    }

    @Test
    @DisplayName("특정 부모 폴더 아래 같은 이름의 활성 파일 존재 여부 확인")
    void existsByProjectIdAndParentFileIdAndNameAndStatus() {
        Project project = createProject();
        ProjectFile rootDirectory = createRootDirectory(project);

        ProjectFile packageJson = createFile(
                project,
                rootDirectory,
                "package.json",
                "/package.json",
                "application/json",
                120L,
                ProjectFileStatus.ACTIVE
        );

        projectFileRepository.save(packageJson);

        boolean exists =
                projectFileRepository
                        .existsByProjectIdAndParentFileIdAndNameAndStatus(
                                project.getId(),
                                rootDirectory.getId(),
                                "package.json",
                                ProjectFileStatus.ACTIVE
                        );

        boolean notExists =
                projectFileRepository
                        .existsByProjectIdAndParentFileIdAndNameAndStatus(
                                project.getId(),
                                rootDirectory.getId(),
                                "README.md",
                                ProjectFileStatus.ACTIVE
                        );

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    private Project createProject() {
        String uniqueValue = UUID.randomUUID().toString();

        User user = User.builder()
                .email("project-file-" + uniqueValue + "@test.com")
                .nickname("파일테스터")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        Runtime runtime = Runtime.builder()
                .name("node-20-" + uniqueValue)
                .displayName("Node.js 20")
                .version("20")
                .dockerImage("node:20")
                .language(RuntimeLanguage.NODE)
                .status(RuntimeStatus.ACTIVE)
                .build();

        Runtime savedRuntime = runtimeRepository.save(runtime);

        Project project = Project.builder()
                .ownerUser(savedUser)
                .guestSession(null)
                .runtime(savedRuntime)
                .name("project-file-test")
                .description("ProjectFile 테스트 프로젝트")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/project-file-test-" + uniqueValue)
                .build();

        return projectRepository.save(project);
    }

    private ProjectFile createRootDirectory(Project project) {
        ProjectFile rootDirectory =
                ProjectFile.createRootDirectory(project);

        return projectFileRepository.save(rootDirectory);
    }

    private ProjectFile createDirectory(
            Project project,
            ProjectFile parentFile,
            String name,
            String path,
            ProjectFileStatus status
    ) {
        return ProjectFile.builder()
                .project(project)
                .parentFile(parentFile)
                .name(name)
                .path(path)
                .fileType(ProjectFileType.DIRECTORY)
                .mimeType(null)
                .sizeBytes(0L)
                .status(status)
                .build();
    }

    private ProjectFile createFile(
            Project project,
            ProjectFile parentFile,
            String name,
            String path,
            String mimeType,
            Long sizeBytes,
            ProjectFileStatus status
    ) {
        return ProjectFile.builder()
                .project(project)
                .parentFile(parentFile)
                .name(name)
                .path(path)
                .fileType(ProjectFileType.FILE)
                .mimeType(mimeType)
                .sizeBytes(sizeBytes)
                .status(status)
                .build();
    }
}