package com.yuhyeon.devwebide.project.service;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectFile;
import com.yuhyeon.devwebide.project.domain.ProjectFileStatus;
import com.yuhyeon.devwebide.project.domain.ProjectFileType;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.dto.ProjectFileContentResponse;
import com.yuhyeon.devwebide.project.dto.ProjectFileCreateRequest;
import com.yuhyeon.devwebide.project.dto.ProjectFileCreateResponse;
import com.yuhyeon.devwebide.project.dto.ProjectFileDeleteResponse;
import com.yuhyeon.devwebide.project.dto.ProjectFileRenameRequest;
import com.yuhyeon.devwebide.project.dto.ProjectFileTreeResponse;
import com.yuhyeon.devwebide.project.repository.FileVersionRepository;
import com.yuhyeon.devwebide.project.repository.ProjectFileRepository;
import com.yuhyeon.devwebide.project.repository.ProjectRepository;
import com.yuhyeon.devwebide.project.repository.ProjectSaveBatchRepository;
import com.yuhyeon.devwebide.user.repository.GuestSessionRepository;
import com.yuhyeon.devwebide.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import com.yuhyeon.devwebide.project.domain.FileVersion;
import com.yuhyeon.devwebide.project.domain.ProjectSaveBatch;
import com.yuhyeon.devwebide.project.domain.ProjectSaveBatchStatus;
import com.yuhyeon.devwebide.project.dto.ProjectFileSaveItemRequest;
import com.yuhyeon.devwebide.project.dto.ProjectFileSaveRequest;
import com.yuhyeon.devwebide.project.dto.ProjectFileSaveResponse;
import com.yuhyeon.devwebide.project.repository.FileVersionRepository;
import com.yuhyeon.devwebide.project.repository.ProjectSaveBatchRepository;
import com.yuhyeon.devwebide.user.domain.GuestSession;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.repository.GuestSessionRepository;
import com.yuhyeon.devwebide.user.repository.UserRepository;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class ProjectFileServiceTest {

    @InjectMocks
    private ProjectFileService projectFileService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectFileRepository projectFileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GuestSessionRepository guestSessionRepository;

    @Mock
    private ProjectSaveBatchRepository projectSaveBatchRepository;

    @Mock
    private FileVersionRepository fileVersionRepository;

    @Mock
    private ProjectFileStorageService projectFileStorageService;

    @Test
    @DisplayName("프로젝트 파일 트리를 조회한다")
    void getFileTree() {
        // given
        Long projectId = 1L;

        Project project = mock(Project.class);

        given(project.isActive())
                .willReturn(true);

        given(projectRepository.findByIdAndStatus(
                projectId,
                ProjectStatus.ACTIVE
        )).willReturn(Optional.of(project));

        ProjectFile root = createDirectory(
                project,
                null,
                1L,
                "/",
                "/"
        );

        ProjectFile src = createDirectory(
                project,
                root,
                2L,
                "src",
                "/src"
        );

        ProjectFile app = createFile(
                project,
                src,
                3L,
                "App.jsx",
                "/src/App.jsx",
                "text/javascript",
                120L
        );

        ProjectFile readme = createFile(
                project,
                root,
                4L,
                "README.md",
                "/README.md",
                "text/markdown",
                40L
        );

        given(projectFileRepository.findByProjectIdAndStatusOrderByPathAsc(
                projectId,
                ProjectFileStatus.ACTIVE
        )).willReturn(List.of(root, readme, src, app));

        // when
        List<ProjectFileTreeResponse> responses =
                projectFileService.getFileTree(projectId);

        // then
        assertThat(responses).hasSize(1);

        ProjectFileTreeResponse rootResponse = responses.get(0);

        assertThat(rootResponse.id()).isEqualTo(1L);
        assertThat(rootResponse.parentFileId()).isNull();
        assertThat(rootResponse.name()).isEqualTo("/");
        assertThat(rootResponse.path()).isEqualTo("/");
        assertThat(rootResponse.fileType()).isEqualTo(ProjectFileType.DIRECTORY);
        assertThat(rootResponse.children()).hasSize(2);

        ProjectFileTreeResponse srcResponse = rootResponse.children()
                .stream()
                .filter(child -> "/src".equals(child.path()))
                .findFirst()
                .orElseThrow();

        assertThat(srcResponse.id()).isEqualTo(2L);
        assertThat(srcResponse.name()).isEqualTo("src");
        assertThat(srcResponse.fileType()).isEqualTo(ProjectFileType.DIRECTORY);
        assertThat(srcResponse.children()).hasSize(1);

        ProjectFileTreeResponse appResponse = srcResponse.children().get(0);

        assertThat(appResponse.id()).isEqualTo(3L);
        assertThat(appResponse.name()).isEqualTo("App.jsx");
        assertThat(appResponse.path()).isEqualTo("/src/App.jsx");
        assertThat(appResponse.fileType()).isEqualTo(ProjectFileType.FILE);
        assertThat(appResponse.mimeType()).isEqualTo("text/javascript");
        assertThat(appResponse.sizeBytes()).isEqualTo(120L);

        ProjectFileTreeResponse readmeResponse = rootResponse.children()
                .stream()
                .filter(child -> "/README.md".equals(child.path()))
                .findFirst()
                .orElseThrow();

        assertThat(readmeResponse.id()).isEqualTo(4L);
        assertThat(readmeResponse.name()).isEqualTo("README.md");
        assertThat(readmeResponse.fileType()).isEqualTo(ProjectFileType.FILE);
        assertThat(readmeResponse.children()).isEmpty();

        then(projectRepository).should()
                .findByIdAndStatus(projectId, ProjectStatus.ACTIVE);

        then(projectFileRepository).should()
                .findByProjectIdAndStatusOrderByPathAsc(
                        projectId,
                        ProjectFileStatus.ACTIVE
                );
    }

    @Test
    @DisplayName("ACTIVE 상태의 파일과 폴더만 조회한다")
    void getFileTree_activeFilesOnly() {
        // given
        Long projectId = 1L;

        Project project = mock(Project.class);

        given(project.isActive())
                .willReturn(true);

        given(projectRepository.findByIdAndStatus(
                projectId,
                ProjectStatus.ACTIVE
        )).willReturn(Optional.of(project));

        ProjectFile root = createDirectory(
                project,
                null,
                1L,
                "/",
                "/"
        );

        given(projectFileRepository.findByProjectIdAndStatusOrderByPathAsc(
                projectId,
                ProjectFileStatus.ACTIVE
        )).willReturn(List.of(root));

        // when
        List<ProjectFileTreeResponse> responses =
                projectFileService.getFileTree(projectId);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).status()).isEqualTo(ProjectFileStatus.ACTIVE);

        then(projectFileRepository).should()
                .findByProjectIdAndStatusOrderByPathAsc(
                        projectId,
                        ProjectFileStatus.ACTIVE
                );
    }

    @Test
    @DisplayName("프로젝트 파일이 없으면 빈 목록을 반환한다")
    void getFileTree_emptyFiles() {
        // given
        Long projectId = 1L;

        Project project = mock(Project.class);

        given(project.isActive())
                .willReturn(true);

        given(projectRepository.findByIdAndStatus(
                projectId,
                ProjectStatus.ACTIVE
        )).willReturn(Optional.of(project));

        given(projectFileRepository.findByProjectIdAndStatusOrderByPathAsc(
                projectId,
                ProjectFileStatus.ACTIVE
        )).willReturn(List.of());

        // when
        List<ProjectFileTreeResponse> responses =
                projectFileService.getFileTree(projectId);

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트면 예외가 발생한다")
    void getFileTree_projectNotFound() {
        // given
        Long projectId = 999L;

        given(projectRepository.findByIdAndStatus(
                projectId,
                ProjectStatus.ACTIVE
        )).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectFileService.getFileTree(projectId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("프로젝트를 찾을 수 없습니다.");

        verifyNoInteractions(projectFileRepository);
    }

    @Test
    @DisplayName("활성 프로젝트가 아니면 예외가 발생한다")
    void getFileTree_notActiveProject() {
        // given
        Long projectId = 1L;

        Project project = mock(Project.class);

        given(project.isActive())
                .willReturn(false);

        given(projectRepository.findByIdAndStatus(
                projectId,
                ProjectStatus.ACTIVE
        )).willReturn(Optional.of(project));

        // when & then
        assertThatThrownBy(() -> projectFileService.getFileTree(projectId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("활성 프로젝트만 파일 트리를 조회할 수 있습니다.");

        verifyNoInteractions(projectFileRepository);
    }

    @Test
    @DisplayName("파일 내용을 조회한다")
    void getFileContent() {
        // given
        Long projectId = 1L;
        Long fileId = 10L;
        LocalDateTime updatedAt = LocalDateTime.of(2026, 7, 7, 10, 0);

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        ProjectFile projectFile = createFile(
                project,
                null,
                fileId,
                "App.jsx",
                "/src/App.jsx",
                "text/javascript",
                120L
        );
        ReflectionTestUtils.setField(projectFile, "updatedAt", updatedAt);

        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(projectFile));

        given(projectFileStorageService.read(project, projectFile))
                .willReturn("console.log('hello');");

        // when
        ProjectFileContentResponse response =
                projectFileService.getFileContent(projectId, fileId);

        // then
        assertThat(response.projectFileId()).isEqualTo(fileId);
        assertThat(response.name()).isEqualTo("App.jsx");
        assertThat(response.path()).isEqualTo("/src/App.jsx");
        assertThat(response.mimeType()).isEqualTo("text/javascript");
        assertThat(response.sizeBytes()).isEqualTo(120L);
        assertThat(response.content()).isEqualTo("console.log('hello');");
        assertThat(response.updatedAt()).isEqualTo(updatedAt);

        then(projectRepository).should()
                .findByIdAndStatus(projectId, ProjectStatus.ACTIVE);

        then(projectFileStorageService).should()
                .read(project, projectFile);
    }

    @Test
    @DisplayName("파일 내용 조회 시 프로젝트가 없으면 예외가 발생한다")
    void getFileContent_projectNotFound() {
        // given
        Long projectId = 999L;
        Long fileId = 10L;

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectFileService.getFileContent(projectId, fileId))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(projectFileRepository);
        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("파일 내용 조회 시 파일이 없으면 예외가 발생한다")
    void getFileContent_fileNotFound() {
        // given
        Long projectId = 1L;
        Long fileId = 999L;

        Project project = mock(Project.class);
        given(project.isActive()).willReturn(true);

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectFileService.getFileContent(projectId, fileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로젝트 파일입니다.");

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("파일 내용 조회 시 다른 프로젝트 파일이면 예외가 발생한다")
    void getFileContent_projectFileMismatch() {
        // given
        Long projectId = 1L;
        Long fileId = 10L;

        Project project = mock(Project.class);
        given(project.isActive()).willReturn(true);

        Project anotherProject = mock(Project.class);
        given(anotherProject.getId()).willReturn(2L);

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        ProjectFile projectFile = createFile(
                anotherProject,
                null,
                fileId,
                "App.jsx",
                "/src/App.jsx",
                "text/javascript",
                120L
        );

        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(projectFile));

        // when & then
        assertThatThrownBy(() -> projectFileService.getFileContent(projectId, fileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 프로젝트에 속한 파일이 아닙니다.");

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("파일 내용 조회 시 삭제된 파일이면 예외가 발생한다")
    void getFileContent_deletedFile() {
        // given
        Long projectId = 1L;
        Long fileId = 10L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        ProjectFile projectFile = createProjectFile(
                project,
                null,
                fileId,
                "App.jsx",
                "/src/App.jsx",
                ProjectFileType.FILE,
                "text/javascript",
                120L,
                ProjectFileStatus.DELETED
        );

        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(projectFile));

        // when & then
        assertThatThrownBy(() -> projectFileService.getFileContent(projectId, fileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제된 파일은 조회할 수 없습니다.");

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("파일 내용 조회 시 디렉터리이면 예외가 발생한다")
    void getFileContent_directory() {
        // given
        Long projectId = 1L;
        Long fileId = 10L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        ProjectFile directory = createDirectory(
                project,
                null,
                fileId,
                "src",
                "/src"
        );

        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(directory));

        // when & then
        assertThatThrownBy(() -> projectFileService.getFileContent(projectId, fileId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("디렉터리는 내용을 조회할 수 없습니다.");

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("프로젝트 파일을 생성한다")
    void createFile_success() {
        // given
        Long projectId = 1L;
        Long rootFileId = 1L;
        Long projectFileId = 10L;
        LocalDateTime now = LocalDateTime.of(2026, 7, 7, 10, 0);

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile root = createDirectory(project, null, rootFileId, "/", "/");

        ProjectFileCreateRequest request = new ProjectFileCreateRequest(
                null,
                "App.jsx",
                ProjectFileType.FILE
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        given(projectFileRepository.findByProjectIdAndParentFileIsNullAndStatus(
                projectId,
                ProjectFileStatus.ACTIVE
        )).willReturn(Optional.of(root));

        given(projectFileRepository.existsByProjectIdAndParentFileIdAndNameAndStatus(
                projectId,
                rootFileId,
                "App.jsx",
                ProjectFileStatus.ACTIVE
        )).willReturn(false);

        given(projectFileRepository.save(any(ProjectFile.class)))
                .willAnswer(invocation -> {
                    ProjectFile projectFile = invocation.getArgument(0);
                    ReflectionTestUtils.setField(projectFile, "id", projectFileId);
                    ReflectionTestUtils.setField(projectFile, "createdAt", now);
                    ReflectionTestUtils.setField(projectFile, "updatedAt", now);
                    return projectFile;
                });

        // when
        ProjectFileCreateResponse response =
                projectFileService.createFile(projectId, request);

        // then
        assertThat(response.projectFileId()).isEqualTo(projectFileId);
        assertThat(response.parentFileId()).isEqualTo(rootFileId);
        assertThat(response.name()).isEqualTo("App.jsx");
        assertThat(response.path()).isEqualTo("/App.jsx");
        assertThat(response.fileType()).isEqualTo(ProjectFileType.FILE);
        assertThat(response.mimeType()).isEqualTo("text/javascript");
        assertThat(response.sizeBytes()).isEqualTo(0L);
        assertThat(response.status()).isEqualTo(ProjectFileStatus.ACTIVE);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);

        then(projectRepository).should()
                .findByIdAndStatus(projectId, ProjectStatus.ACTIVE);

        then(projectFileStorageService).should()
                .createFile(eq(project), any(ProjectFile.class));
    }

    @Test
    @DisplayName("프로젝트 디렉터리를 생성한다")
    void createDirectory_success() {
        // given
        Long projectId = 1L;
        Long rootFileId = 1L;
        Long projectFileId = 11L;
        LocalDateTime now = LocalDateTime.of(2026, 7, 7, 10, 0);

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile root = createDirectory(project, null, rootFileId, "/", "/");

        ProjectFileCreateRequest request = new ProjectFileCreateRequest(
                null,
                "src",
                ProjectFileType.DIRECTORY
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        given(projectFileRepository.findByProjectIdAndParentFileIsNullAndStatus(
                projectId,
                ProjectFileStatus.ACTIVE
        )).willReturn(Optional.of(root));

        given(projectFileRepository.existsByProjectIdAndParentFileIdAndNameAndStatus(
                projectId,
                rootFileId,
                "src",
                ProjectFileStatus.ACTIVE
        )).willReturn(false);

        given(projectFileRepository.save(any(ProjectFile.class)))
                .willAnswer(invocation -> {
                    ProjectFile projectFile = invocation.getArgument(0);
                    ReflectionTestUtils.setField(projectFile, "id", projectFileId);
                    ReflectionTestUtils.setField(projectFile, "createdAt", now);
                    ReflectionTestUtils.setField(projectFile, "updatedAt", now);
                    return projectFile;
                });

        // when
        ProjectFileCreateResponse response =
                projectFileService.createFile(projectId, request);

        // then
        assertThat(response.projectFileId()).isEqualTo(projectFileId);
        assertThat(response.parentFileId()).isEqualTo(rootFileId);
        assertThat(response.name()).isEqualTo("src");
        assertThat(response.path()).isEqualTo("/src");
        assertThat(response.fileType()).isEqualTo(ProjectFileType.DIRECTORY);
        assertThat(response.mimeType()).isNull();
        assertThat(response.sizeBytes()).isEqualTo(0L);
        assertThat(response.status()).isEqualTo(ProjectFileStatus.ACTIVE);

        then(projectFileStorageService).should()
                .createDirectory(eq(project), any(ProjectFile.class));
    }

    @Test
    @DisplayName("부모 디렉터리 하위에 파일을 생성한다")
    void createFile_underParentDirectory() {
        // given
        Long projectId = 1L;
        Long parentFileId = 2L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile src = createDirectory(project, null, parentFileId, "src", "/src");

        ProjectFileCreateRequest request = new ProjectFileCreateRequest(
                parentFileId,
                "index.ts",
                ProjectFileType.FILE
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        given(projectFileRepository.findById(parentFileId))
                .willReturn(Optional.of(src));

        given(projectFileRepository.existsByProjectIdAndParentFileIdAndNameAndStatus(
                projectId,
                parentFileId,
                "index.ts",
                ProjectFileStatus.ACTIVE
        )).willReturn(false);

        given(projectFileRepository.save(any(ProjectFile.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        ProjectFileCreateResponse response =
                projectFileService.createFile(projectId, request);

        // then
        assertThat(response.parentFileId()).isEqualTo(parentFileId);
        assertThat(response.path()).isEqualTo("/src/index.ts");
        assertThat(response.mimeType()).isEqualTo("text/typescript");
    }

    @Test
    @DisplayName("부모 파일이 없으면 예외가 발생한다")
    void createFile_parentNotFound() {
        // given
        Long projectId = 1L;
        Long parentFileId = 999L;

        Project project = mock(Project.class);
        given(project.isActive()).willReturn(true);

        ProjectFileCreateRequest request = new ProjectFileCreateRequest(
                parentFileId,
                "App.jsx",
                ProjectFileType.FILE
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        given(projectFileRepository.findById(parentFileId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectFileService.createFile(projectId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("부모 파일을 찾을 수 없습니다.");

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("부모 파일이 다른 프로젝트 소속이면 예외가 발생한다")
    void createFile_parentProjectMismatch() {
        // given
        Long projectId = 1L;
        Long parentFileId = 2L;

        Project project = mock(Project.class);
        given(project.isActive()).willReturn(true);

        Project anotherProject = mock(Project.class);
        given(anotherProject.getId()).willReturn(2L);

        ProjectFile parentFile = createDirectory(
                anotherProject,
                null,
                parentFileId,
                "src",
                "/src"
        );

        ProjectFileCreateRequest request = new ProjectFileCreateRequest(
                parentFileId,
                "App.jsx",
                ProjectFileType.FILE
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        given(projectFileRepository.findById(parentFileId))
                .willReturn(Optional.of(parentFile));

        // when & then
        assertThatThrownBy(() -> projectFileService.createFile(projectId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 프로젝트에 속한 부모 파일이 아닙니다.");

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("삭제된 부모 파일이면 예외가 발생한다")
    void createFile_deletedParent() {
        // given
        Long projectId = 1L;
        Long parentFileId = 2L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile parentFile = createProjectFile(
                project,
                null,
                parentFileId,
                "src",
                "/src",
                ProjectFileType.DIRECTORY,
                null,
                0L,
                ProjectFileStatus.DELETED
        );

        ProjectFileCreateRequest request = new ProjectFileCreateRequest(
                parentFileId,
                "App.jsx",
                ProjectFileType.FILE
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        given(projectFileRepository.findById(parentFileId))
                .willReturn(Optional.of(parentFile));

        // when & then
        assertThatThrownBy(() -> projectFileService.createFile(projectId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제된 폴더에는 파일을 생성할 수 없습니다.");

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("부모가 파일이면 예외가 발생한다")
    void createFile_parentIsFile() {
        // given
        Long projectId = 1L;
        Long parentFileId = 2L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile parentFile = createFile(
                project,
                null,
                parentFileId,
                "README.md",
                "/README.md",
                "text/markdown",
                10L
        );

        ProjectFileCreateRequest request = new ProjectFileCreateRequest(
                parentFileId,
                "App.jsx",
                ProjectFileType.FILE
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        given(projectFileRepository.findById(parentFileId))
                .willReturn(Optional.of(parentFile));

        // when & then
        assertThatThrownBy(() -> projectFileService.createFile(projectId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("디렉터리 하위에만 파일을 생성할 수 있습니다.");

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("파일명이 null이면 예외가 발생한다")
    void createFile_nullName() {
        assertInvalidFileName(null);
    }

    @Test
    @DisplayName("파일명이 blank이면 예외가 발생한다")
    void createFile_blankName() {
        assertInvalidFileName(" ");
    }

    @Test
    @DisplayName("파일명에 슬래시가 있으면 예외가 발생한다")
    void createFile_nameContainsSlash() {
        assertInvalidFileName("src/App.jsx");
    }

    @Test
    @DisplayName("파일명에 역슬래시가 있으면 예외가 발생한다")
    void createFile_nameContainsBackslash() {
        assertInvalidFileName("src\\App.jsx");
    }

    @Test
    @DisplayName("파일명에 상위 경로가 있으면 예외가 발생한다")
    void createFile_nameContainsParentPath() {
        assertInvalidFileName("..env");
    }

    @Test
    @DisplayName("같은 부모 아래 동일 이름이 있으면 예외가 발생한다")
    void createFile_duplicateName() {
        // given
        Long projectId = 1L;
        Long rootFileId = 1L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile root = createDirectory(project, null, rootFileId, "/", "/");

        ProjectFileCreateRequest request = new ProjectFileCreateRequest(
                null,
                "App.jsx",
                ProjectFileType.FILE
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        given(projectFileRepository.findByProjectIdAndParentFileIsNullAndStatus(
                projectId,
                ProjectFileStatus.ACTIVE
        )).willReturn(Optional.of(root));

        given(projectFileRepository.existsByProjectIdAndParentFileIdAndNameAndStatus(
                projectId,
                rootFileId,
                "App.jsx",
                ProjectFileStatus.ACTIVE
        )).willReturn(true);

        // when & then
        assertThatThrownBy(() -> projectFileService.createFile(projectId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("같은 위치에 동일한 이름의 파일 또는 폴더가 이미 존재합니다.");

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("?뚯씪 ?대쫫??蹂寃쏀븳??")
    void renameFile_success() {
        Long projectId = 1L;
        Long parentFileId = 2L;
        Long fileId = 10L;
        LocalDateTime now = LocalDateTime.of(2026, 7, 7, 10, 0);

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile parentFile = createDirectory(project, null, parentFileId, "src", "/src");
        ProjectFile projectFile = createFile(
                project,
                parentFile,
                fileId,
                "App.jsx",
                "/src/App.jsx",
                "text/javascript",
                120L
        );
        ReflectionTestUtils.setField(projectFile, "createdAt", now);
        ReflectionTestUtils.setField(projectFile, "updatedAt", now);

        ProjectFileRenameRequest request = new ProjectFileRenameRequest("App.tsx");

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(projectFile));
        given(projectFileRepository.existsByProjectIdAndParentFileIdAndNameAndStatus(
                projectId,
                parentFileId,
                "App.tsx",
                ProjectFileStatus.ACTIVE
        )).willReturn(false);

        ProjectFileCreateResponse response =
                projectFileService.renameFile(projectId, fileId, request);

        assertThat(response.projectFileId()).isEqualTo(fileId);
        assertThat(response.parentFileId()).isEqualTo(parentFileId);
        assertThat(response.name()).isEqualTo("App.tsx");
        assertThat(response.path()).isEqualTo("/src/App.tsx");
        assertThat(response.fileType()).isEqualTo(ProjectFileType.FILE);
        assertThat(response.mimeType()).isEqualTo("text/typescript");
        assertThat(response.sizeBytes()).isEqualTo(120L);
        assertThat(response.status()).isEqualTo(ProjectFileStatus.ACTIVE);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);

        assertThat(projectFile.getName()).isEqualTo("App.tsx");
        assertThat(projectFile.getPath()).isEqualTo("/src/App.tsx");
        assertThat(projectFile.getMimeType()).isEqualTo("text/typescript");

        then(projectRepository).should()
                .findByIdAndStatus(projectId, ProjectStatus.ACTIVE);
        then(projectFileStorageService).should()
                .rename(project, projectFile, "/src/App.tsx");
    }

    @Test
    @DisplayName("?붾젆?곕━ ?대쫫??蹂寃쏀븳??")
    void renameDirectory_success() {
        Long projectId = 1L;
        Long rootFileId = 1L;
        Long directoryId = 10L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile root = createDirectory(project, null, rootFileId, "/", "/");
        ProjectFile directory = createDirectory(project, root, directoryId, "src", "/src");

        ProjectFileRenameRequest request = new ProjectFileRenameRequest("app");

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(directoryId))
                .willReturn(Optional.of(directory));
        given(projectFileRepository.existsByProjectIdAndParentFileIdAndNameAndStatus(
                projectId,
                rootFileId,
                "app",
                ProjectFileStatus.ACTIVE
        )).willReturn(false);
        given(projectFileRepository.findByProjectIdAndStatusAndPathStartingWithOrderByPathAsc(
                projectId,
                ProjectFileStatus.ACTIVE,
                "/src/"
        )).willReturn(List.of());

        ProjectFileCreateResponse response =
                projectFileService.renameFile(projectId, directoryId, request);

        assertThat(response.projectFileId()).isEqualTo(directoryId);
        assertThat(response.parentFileId()).isEqualTo(rootFileId);
        assertThat(response.name()).isEqualTo("app");
        assertThat(response.path()).isEqualTo("/app");
        assertThat(response.fileType()).isEqualTo(ProjectFileType.DIRECTORY);
        assertThat(response.mimeType()).isNull();
        assertThat(response.sizeBytes()).isEqualTo(0L);

        then(projectFileStorageService).should()
                .rename(project, directory, "/app");
    }

    @Test
    @DisplayName("?붾젆?곕━ ?대쫫 蹂寃???하위 ?뚯씪 寃쎈줈瑜?媛깆떊?쒕떎")
    void renameDirectory_updatesDescendantPaths() {
        Long projectId = 1L;
        Long rootFileId = 1L;
        Long directoryId = 10L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile root = createDirectory(project, null, rootFileId, "/", "/");
        ProjectFile directory = createDirectory(project, root, directoryId, "src", "/src");
        ProjectFile childDirectory = createDirectory(project, directory, 11L, "components", "/src/components");
        ProjectFile childFile = createFile(
                project,
                childDirectory,
                12L,
                "Button.jsx",
                "/src/components/Button.jsx",
                "text/javascript",
                30L
        );

        ProjectFileRenameRequest request = new ProjectFileRenameRequest("app");

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(directoryId))
                .willReturn(Optional.of(directory));
        given(projectFileRepository.existsByProjectIdAndParentFileIdAndNameAndStatus(
                projectId,
                rootFileId,
                "app",
                ProjectFileStatus.ACTIVE
        )).willReturn(false);
        given(projectFileRepository.findByProjectIdAndStatusAndPathStartingWithOrderByPathAsc(
                projectId,
                ProjectFileStatus.ACTIVE,
                "/src/"
        )).willReturn(List.of(childDirectory, childFile));

        projectFileService.renameFile(projectId, directoryId, request);

        assertThat(directory.getPath()).isEqualTo("/app");
        assertThat(childDirectory.getPath()).isEqualTo("/app/components");
        assertThat(childFile.getPath()).isEqualTo("/app/components/Button.jsx");
    }

    @Test
    @DisplayName("?대쫫 蹂寃????뚯씪???놁쑝硫??덉쇅媛 諛쒖깮?쒕떎")
    void renameFile_notFound() {
        Long projectId = 1L;
        Long fileId = 999L;

        Project project = mock(Project.class);
        given(project.isActive()).willReturn(true);

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> projectFileService.renameFile(
                projectId,
                fileId,
                new ProjectFileRenameRequest("App.tsx")
        )).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("?대쫫 蹂寃????ㅻⅨ ?꾨줈?앺듃 ?뚯씪?대㈃ ?덉쇅媛 諛쒖깮?쒕떎")
    void renameFile_projectMismatch() {
        Long projectId = 1L;
        Long fileId = 10L;

        Project project = mock(Project.class);
        given(project.isActive()).willReturn(true);

        Project anotherProject = mock(Project.class);
        given(anotherProject.getId()).willReturn(2L);

        ProjectFile projectFile = createFile(
                anotherProject,
                null,
                fileId,
                "App.jsx",
                "/App.jsx",
                "text/javascript",
                120L
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(projectFile));

        assertThatThrownBy(() -> projectFileService.renameFile(
                projectId,
                fileId,
                new ProjectFileRenameRequest("App.tsx")
        )).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("?대쫫 蹂寃????뚯씪???젣 ?곹깭硫??덉쇅媛 諛쒖깮?쒕떎")
    void renameFile_deletedFile() {
        Long projectId = 1L;
        Long fileId = 10L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile projectFile = createProjectFile(
                project,
                null,
                fileId,
                "App.jsx",
                "/App.jsx",
                ProjectFileType.FILE,
                "text/javascript",
                120L,
                ProjectFileStatus.DELETED
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(projectFile));

        assertThatThrownBy(() -> projectFileService.renameFile(
                projectId,
                fileId,
                new ProjectFileRenameRequest("App.tsx")
        )).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("猷⑦듃 ?붾젆?곕━???대쫫??蹂寃쏀븷 ???녿떎")
    void renameFile_rootDirectory() {
        Long projectId = 1L;
        Long fileId = 1L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile root = createDirectory(project, null, fileId, "/", "/");

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(root));

        assertThatThrownBy(() -> projectFileService.renameFile(
                projectId,
                fileId,
                new ProjectFileRenameRequest("root")
        )).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("?대쫫 蹂寃????뚯씪紐낆씠 null?대㈃ ?덉쇅媛 諛쒖깮?쒕떎")
    void renameFile_nullName() {
        assertInvalidRenameName(null);
    }

    @Test
    @DisplayName("?대쫫 蹂寃????뚯씪紐낆씠 blank?대㈃ ?덉쇅媛 諛쒖깮?쒕떎")
    void renameFile_blankName() {
        assertInvalidRenameName(" ");
    }

    @Test
    @DisplayName("?대쫫 蹂寃????뚯씪紐낆뿉 ?щ옒?쒓? ?덉쑝硫??덉쇅媛 諛쒖깮?쒕떎")
    void renameFile_nameContainsSlash() {
        assertInvalidRenameName("src/App.jsx");
    }

    @Test
    @DisplayName("?대쫫 蹂寃????뚯씪紐낆뿉 ??뒳?섏떆媛 ?덉쑝硫??덉쇅媛 諛쒖깮?쒕떎")
    void renameFile_nameContainsBackslash() {
        assertInvalidRenameName("src\\App.jsx");
    }

    @Test
    @DisplayName("?대쫫 蹂寃????뚯씪紐낆뿉 ?곸쐞 寃쎈줈媛 ?덉쑝硫??덉쇅媛 諛쒖깮?쒕떎")
    void renameFile_nameContainsParentPath() {
        assertInvalidRenameName("..env");
    }

    @Test
    @DisplayName("?대쫫 蹂寃???媛숈? 遺紐??꾨옒 ?숈씪 ?대쫫???덉쑝硫??덉쇅媛 諛쒖깮?쒕떎")
    void renameFile_duplicateName() {
        Long projectId = 1L;
        Long rootFileId = 1L;
        Long fileId = 10L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile root = createDirectory(project, null, rootFileId, "/", "/");
        ProjectFile projectFile = createFile(
                project,
                root,
                fileId,
                "App.jsx",
                "/App.jsx",
                "text/javascript",
                120L
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(projectFile));
        given(projectFileRepository.existsByProjectIdAndParentFileIdAndNameAndStatus(
                projectId,
                rootFileId,
                "README.md",
                ProjectFileStatus.ACTIVE
        )).willReturn(true);

        assertThatThrownBy(() -> projectFileService.renameFile(
                projectId,
                fileId,
                new ProjectFileRenameRequest("README.md")
        )).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("?숈씪 ?대쫫?쇰줈 蹂寃??붿껌?섎㈃ ?꾩옱 硫뷀??곗씠?곕? 諛섑솚?쒕떎")
    void renameFile_sameName() {
        Long projectId = 1L;
        Long rootFileId = 1L;
        Long fileId = 10L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile root = createDirectory(project, null, rootFileId, "/", "/");
        ProjectFile projectFile = createFile(
                project,
                root,
                fileId,
                "App.jsx",
                "/App.jsx",
                "text/javascript",
                120L
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(projectFile));

        ProjectFileCreateResponse response = projectFileService.renameFile(
                projectId,
                fileId,
                new ProjectFileRenameRequest("App.jsx")
        );

        assertThat(response.projectFileId()).isEqualTo(fileId);
        assertThat(response.name()).isEqualTo("App.jsx");
        assertThat(response.path()).isEqualTo("/App.jsx");

        verifyNoInteractions(projectFileStorageService);
    }

    private void assertInvalidRenameName(String name) {
        Long projectId = 1L;
        Long rootFileId = 1L;
        Long fileId = 10L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile root = createDirectory(project, null, rootFileId, "/", "/");
        ProjectFile projectFile = createFile(
                project,
                root,
                fileId,
                "App.jsx",
                "/App.jsx",
                "text/javascript",
                120L
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(projectFile));

        assertThatThrownBy(() -> projectFileService.renameFile(
                projectId,
                fileId,
                new ProjectFileRenameRequest(name)
        )).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("프로젝트 파일을 삭제한다")
    void deleteFile_success() {
        Long projectId = 1L;
        Long fileId = 10L;
        LocalDateTime updatedAt = LocalDateTime.of(2026, 7, 8, 10, 0);

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile projectFile = createFile(
                project,
                null,
                fileId,
                "App.jsx",
                "/App.jsx",
                "text/javascript",
                120L
        );
        ReflectionTestUtils.setField(projectFile, "updatedAt", updatedAt);

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(projectFile));

        ProjectFileDeleteResponse response =
                projectFileService.deleteFile(projectId, fileId);

        assertThat(projectFile.getStatus()).isEqualTo(ProjectFileStatus.DELETED);
        assertThat(response.projectFileId()).isEqualTo(fileId);
        assertThat(response.fileType()).isEqualTo(ProjectFileType.FILE);
        assertThat(response.path()).isEqualTo("/App.jsx");
        assertThat(response.status()).isEqualTo(ProjectFileStatus.DELETED);
        assertThat(response.deleted()).isTrue();
        assertThat(response.updatedAt()).isEqualTo(updatedAt);

        then(projectRepository).should()
                .findByIdAndStatus(projectId, ProjectStatus.ACTIVE);
        then(projectFileStorageService).should()
                .delete(project, projectFile);
    }

    @Test
    @DisplayName("프로젝트 디렉터리를 삭제한다")
    void deleteDirectory_success() {
        Long projectId = 1L;
        Long directoryId = 10L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile directory = createDirectory(project, null, directoryId, "src", "/src");

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(directoryId))
                .willReturn(Optional.of(directory));
        given(projectFileRepository.findByProjectIdAndStatusAndPathStartingWithOrderByPathAsc(
                projectId,
                ProjectFileStatus.ACTIVE,
                "/src/"
        )).willReturn(List.of());

        ProjectFileDeleteResponse response =
                projectFileService.deleteFile(projectId, directoryId);

        assertThat(directory.getStatus()).isEqualTo(ProjectFileStatus.DELETED);
        assertThat(response.projectFileId()).isEqualTo(directoryId);
        assertThat(response.fileType()).isEqualTo(ProjectFileType.DIRECTORY);
        assertThat(response.path()).isEqualTo("/src");
        assertThat(response.status()).isEqualTo(ProjectFileStatus.DELETED);
        assertThat(response.deleted()).isTrue();

        then(projectFileStorageService).should()
                .delete(project, directory);
    }

    @Test
    @DisplayName("디렉터리 삭제 시 하위 파일과 폴더도 삭제 상태로 변경한다")
    void deleteDirectory_deletesDescendants() {
        Long projectId = 1L;
        Long directoryId = 10L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile directory = createDirectory(project, null, directoryId, "src", "/src");
        ProjectFile childDirectory = createDirectory(project, directory, 11L, "components", "/src/components");
        ProjectFile childFile = createFile(
                project,
                childDirectory,
                12L,
                "Button.jsx",
                "/src/components/Button.jsx",
                "text/javascript",
                30L
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(directoryId))
                .willReturn(Optional.of(directory));
        given(projectFileRepository.findByProjectIdAndStatusAndPathStartingWithOrderByPathAsc(
                projectId,
                ProjectFileStatus.ACTIVE,
                "/src/"
        )).willReturn(List.of(childDirectory, childFile));

        projectFileService.deleteFile(projectId, directoryId);

        assertThat(directory.getStatus()).isEqualTo(ProjectFileStatus.DELETED);
        assertThat(childDirectory.getStatus()).isEqualTo(ProjectFileStatus.DELETED);
        assertThat(childFile.getStatus()).isEqualTo(ProjectFileStatus.DELETED);
    }

    @Test
    @DisplayName("삭제할 파일이 없으면 예외가 발생한다")
    void deleteFile_notFound() {
        Long projectId = 1L;
        Long fileId = 999L;

        Project project = mock(Project.class);
        given(project.isActive()).willReturn(true);

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> projectFileService.deleteFile(projectId, fileId))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("다른 프로젝트 파일은 삭제할 수 없다")
    void deleteFile_projectMismatch() {
        Long projectId = 1L;
        Long fileId = 10L;

        Project project = mock(Project.class);
        given(project.isActive()).willReturn(true);

        Project anotherProject = mock(Project.class);
        given(anotherProject.getId()).willReturn(2L);

        ProjectFile projectFile = createFile(
                anotherProject,
                null,
                fileId,
                "App.jsx",
                "/App.jsx",
                "text/javascript",
                120L
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(projectFile));

        assertThatThrownBy(() -> projectFileService.deleteFile(projectId, fileId))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("이미 삭제된 파일은 다시 삭제할 수 없다")
    void deleteFile_alreadyDeleted() {
        Long projectId = 1L;
        Long fileId = 10L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile projectFile = createProjectFile(
                project,
                null,
                fileId,
                "App.jsx",
                "/App.jsx",
                ProjectFileType.FILE,
                "text/javascript",
                120L,
                ProjectFileStatus.DELETED
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(projectFile));

        assertThatThrownBy(() -> projectFileService.deleteFile(projectId, fileId))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("루트 디렉터리는 삭제할 수 없다")
    void deleteFile_rootDirectory() {
        Long projectId = 1L;
        Long fileId = 1L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile root = createDirectory(project, null, fileId, "/", "/");

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));
        given(projectFileRepository.findById(fileId))
                .willReturn(Optional.of(root));

        assertThatThrownBy(() -> projectFileService.deleteFile(projectId, fileId))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(projectFileStorageService);
    }

    private void assertInvalidFileName(String name) {
        Long projectId = 1L;
        Long rootFileId = 1L;

        Project project = mock(Project.class);
        given(project.getId()).willReturn(projectId);
        given(project.isActive()).willReturn(true);

        ProjectFile root = createDirectory(project, null, rootFileId, "/", "/");

        ProjectFileCreateRequest request = new ProjectFileCreateRequest(
                null,
                name,
                ProjectFileType.FILE
        );

        given(projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE))
                .willReturn(Optional.of(project));

        given(projectFileRepository.findByProjectIdAndParentFileIsNullAndStatus(
                projectId,
                ProjectFileStatus.ACTIVE
        )).willReturn(Optional.of(root));

        String expectedMessage = name == null || name.isBlank()
                ? "파일/폴더 이름은 필수입니다."
                : "사용할 수 없는 파일/폴더 이름입니다.";

        assertThatThrownBy(() -> projectFileService.createFile(projectId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);

        verifyNoInteractions(projectFileStorageService);
    }

    private ProjectFile createDirectory(
            Project project,
            ProjectFile parentFile,
            Long id,
            String name,
            String path
    ) {
        ProjectFile projectFile = ProjectFile.builder()
                .project(project)
                .parentFile(parentFile)
                .name(name)
                .path(path)
                .fileType(ProjectFileType.DIRECTORY)
                .mimeType(null)
                .sizeBytes(0L)
                .status(ProjectFileStatus.ACTIVE)
                .build();

        ReflectionTestUtils.setField(projectFile, "id", id);

        return projectFile;
    }

    private ProjectFile createFile(
            Project project,
            ProjectFile parentFile,
            Long id,
            String name,
            String path,
            String mimeType,
                Long sizeBytes
    ) {
        return createProjectFile(
                project,
                parentFile,
                id,
                name,
                path,
                ProjectFileType.FILE,
                mimeType,
                sizeBytes,
                ProjectFileStatus.ACTIVE
        );
    }

    private ProjectFile createProjectFile(
            Project project,
            ProjectFile parentFile,
            Long id,
            String name,
            String path,
            ProjectFileType fileType,
            String mimeType,
            Long sizeBytes,
            ProjectFileStatus status
    ) {
        ProjectFile projectFile = ProjectFile.builder()
                .project(project)
                .parentFile(parentFile)
                .name(name)
                .path(path)
                .fileType(fileType)
                .mimeType(mimeType)
                .sizeBytes(sizeBytes)
                .status(status)
                .build();

        ReflectionTestUtils.setField(projectFile, "id", id);

        return projectFile;
    }

    @Test
    @DisplayName("회원이 파일을 저장한다")
    void saveFiles_userSuccess() {
        // given
        Long projectId = 1L;
        Long userId = 1L;
        Long projectFileId = 10L;

        Project project = mock(Project.class);

        given(project.getId())
                .willReturn(projectId);

        given(projectRepository.findByIdAndStatus(
                projectId,
                ProjectStatus.ACTIVE
        )).willReturn(Optional.of(project));

        User user = createUser(userId);

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        ProjectFile projectFile = createFile(
                project,
                null,
                projectFileId,
                "App.jsx",
                "/src/App.jsx",
                "text/javascript",
                120L
        );

        given(projectFileRepository.findById(projectFileId))
                .willReturn(Optional.of(projectFile));

        ProjectFileStorageService.StoredFile storedFile =
                mock(ProjectFileStorageService.StoredFile.class);

        given(storedFile.storagePath())
                .willReturn("/projects/1/src/App.jsx");
        given(storedFile.contentHash())
                .willReturn("hash-app-jsx");
        given(storedFile.sizeBytes())
                .willReturn(200L);

        given(projectFileStorageService.save(
                eq(project),
                eq(projectFile),
                eq("console.log('hello');"),
                eq(1)
        )).willReturn(storedFile);

        given(projectSaveBatchRepository.save(any(ProjectSaveBatch.class)))
                .willAnswer(invocation -> {
                    ProjectSaveBatch saveBatch = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saveBatch, "id", 100L);
                    return saveBatch;
                });

        given(fileVersionRepository.findTopByProjectFileIdOrderByVersionNoDesc(
                projectFileId
        )).willReturn(Optional.empty());

        given(fileVersionRepository.save(any(FileVersion.class)))
                .willAnswer(invocation -> {
                    FileVersion fileVersion = invocation.getArgument(0);
                    ReflectionTestUtils.setField(fileVersion, "id", 1000L);
                    return fileVersion;
                });

        ProjectFileSaveRequest request = new ProjectFileSaveRequest(
                userId,
                null,
                List.of(
                        new ProjectFileSaveItemRequest(
                                projectFileId,
                                "console.log('hello');"
                        )
                )
        );

        // when
        ProjectFileSaveResponse response =
                projectFileService.saveFiles(projectId, request);

        // then
        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.saveBatchId()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(ProjectSaveBatchStatus.SUCCESS);
        assertThat(response.savedFileCount()).isEqualTo(1);
        assertThat(response.files()).hasSize(1);

        assertThat(response.files().get(0).projectFileId())
                .isEqualTo(projectFileId);
        assertThat(response.files().get(0).path())
                .isEqualTo("/src/App.jsx");
        assertThat(response.files().get(0).versionNo())
                .isEqualTo(1);
        assertThat(response.files().get(0).sizeBytes())
                .isEqualTo(200L);
        assertThat(response.files().get(0).contentHash())
                .isEqualTo("hash-app-jsx");

        assertThat(projectFile.getSizeBytes()).isEqualTo(200L);
        assertThat(projectFile.getMimeType()).isEqualTo("text/javascript");

        ArgumentCaptor<ProjectSaveBatch> saveBatchCaptor =
                ArgumentCaptor.forClass(ProjectSaveBatch.class);

        then(projectSaveBatchRepository).should()
                .save(saveBatchCaptor.capture());

        ProjectSaveBatch savedBatch = saveBatchCaptor.getValue();

        assertThat(savedBatch.getProject()).isEqualTo(project);
        assertThat(savedBatch.getUser()).isEqualTo(user);
        assertThat(savedBatch.getGuestSession()).isNull();
        assertThat(savedBatch.getStatus())
                .isEqualTo(ProjectSaveBatchStatus.SUCCESS);
        assertThat(savedBatch.getSavedFileCount()).isEqualTo(1);

        then(fileVersionRepository).should()
                .save(any(FileVersion.class));

        then(projectFileStorageService).should()
                .save(project, projectFile, "console.log('hello');", 1);
    }

    @Test
    @DisplayName("게스트가 파일을 저장한다")
    void saveFiles_guestSuccess() {
        // given
        Long projectId = 1L;
        Long guestSessionId = 5L;
        Long projectFileId = 10L;

        Project project = mock(Project.class);

        given(project.getId())
                .willReturn(projectId);

        given(projectRepository.findByIdAndStatus(
                projectId,
                ProjectStatus.ACTIVE
        )).willReturn(Optional.of(project));

        GuestSession guestSession = createGuestSession(guestSessionId);

        given(guestSessionRepository.findById(guestSessionId))
                .willReturn(Optional.of(guestSession));

        ProjectFile projectFile = createFile(
                project,
                null,
                projectFileId,
                "main.py",
                "/main.py",
                "text/x-python",
                50L
        );

        given(projectFileRepository.findById(projectFileId))
                .willReturn(Optional.of(projectFile));

        ProjectFileStorageService.StoredFile storedFile =
                mock(ProjectFileStorageService.StoredFile.class);

        given(storedFile.storagePath())
                .willReturn("/projects/1/main.py");
        given(storedFile.contentHash())
                .willReturn("hash-main-py");
        given(storedFile.sizeBytes())
                .willReturn(80L);

        given(projectFileStorageService.save(
                eq(project),
                eq(projectFile),
                eq("print('hello')"),
                eq(1)
        )).willReturn(storedFile);

        given(projectSaveBatchRepository.save(any(ProjectSaveBatch.class)))
                .willAnswer(invocation -> {
                    ProjectSaveBatch saveBatch = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saveBatch, "id", 101L);
                    return saveBatch;
                });

        given(fileVersionRepository.findTopByProjectFileIdOrderByVersionNoDesc(
                projectFileId
        )).willReturn(Optional.empty());

        given(fileVersionRepository.save(any(FileVersion.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        ProjectFileSaveRequest request = new ProjectFileSaveRequest(
                null,
                guestSessionId,
                List.of(
                        new ProjectFileSaveItemRequest(
                                projectFileId,
                                "print('hello')"
                        )
                )
        );

        // when
        ProjectFileSaveResponse response =
                projectFileService.saveFiles(projectId, request);

        // then
        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.saveBatchId()).isEqualTo(101L);
        assertThat(response.savedFileCount()).isEqualTo(1);

        ArgumentCaptor<ProjectSaveBatch> saveBatchCaptor =
                ArgumentCaptor.forClass(ProjectSaveBatch.class);

        then(projectSaveBatchRepository).should()
                .save(saveBatchCaptor.capture());

        ProjectSaveBatch savedBatch = saveBatchCaptor.getValue();

        assertThat(savedBatch.getUser()).isNull();
        assertThat(savedBatch.getGuestSession()).isEqualTo(guestSession);
    }

    @Test
    @DisplayName("저장 시 userId와 guestSessionId가 모두 없으면 예외가 발생한다")
    void saveFiles_noActor() {
        // given
        Long projectId = 1L;

        ProjectFileSaveRequest request = new ProjectFileSaveRequest(
                null,
                null,
                List.of(
                        new ProjectFileSaveItemRequest(
                                10L,
                                "content"
                        )
                )
        );

        // when & then
        assertThatThrownBy(() -> projectFileService.saveFiles(projectId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId 또는 guestSessionId 중 하나만 전달해야 합니다.");

        verifyNoInteractions(projectRepository);
        verifyNoInteractions(projectFileRepository);
        verifyNoInteractions(projectSaveBatchRepository);
        verifyNoInteractions(fileVersionRepository);
        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("저장 시 userId와 guestSessionId가 모두 있으면 예외가 발생한다")
    void saveFiles_bothActor() {
        // given
        Long projectId = 1L;

        ProjectFileSaveRequest request = new ProjectFileSaveRequest(
                1L,
                1L,
                List.of(
                        new ProjectFileSaveItemRequest(
                                10L,
                                "content"
                        )
                )
        );

        // when & then
        assertThatThrownBy(() -> projectFileService.saveFiles(projectId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId 또는 guestSessionId 중 하나만 전달해야 합니다.");

        verifyNoInteractions(projectRepository);
        verifyNoInteractions(projectFileRepository);
        verifyNoInteractions(projectSaveBatchRepository);
        verifyNoInteractions(fileVersionRepository);
        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트면 파일 저장 시 예외가 발생한다")
    void saveFiles_projectNotFound() {
        // given
        Long projectId = 999L;

        ProjectFileSaveRequest request = new ProjectFileSaveRequest(
                1L,
                null,
                List.of(
                        new ProjectFileSaveItemRequest(
                                10L,
                                "content"
                        )
                )
        );

        given(projectRepository.findByIdAndStatus(
                projectId,
                ProjectStatus.ACTIVE
        )).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectFileService.saveFiles(projectId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않거나 삭제된 프로젝트입니다.");

        verifyNoInteractions(projectFileRepository);
        verifyNoInteractions(projectSaveBatchRepository);
        verifyNoInteractions(fileVersionRepository);
        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("존재하지 않는 파일이면 파일 저장 시 예외가 발생한다")
    void saveFiles_fileNotFound() {
        // given
        Long projectId = 1L;
        Long userId = 1L;
        Long projectFileId = 999L;

        Project project = mock(Project.class);

        given(projectRepository.findByIdAndStatus(
                projectId,
                ProjectStatus.ACTIVE
        )).willReturn(Optional.of(project));

        User user = createUser(userId);

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        given(projectFileRepository.findById(projectFileId))
                .willReturn(Optional.empty());

        ProjectFileSaveRequest request = new ProjectFileSaveRequest(
                userId,
                null,
                List.of(
                        new ProjectFileSaveItemRequest(
                                projectFileId,
                                "content"
                        )
                )
        );

        // when & then
        assertThatThrownBy(() -> projectFileService.saveFiles(projectId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 프로젝트 파일입니다.");

        verifyNoInteractions(projectSaveBatchRepository);
        verifyNoInteractions(fileVersionRepository);
        verifyNoInteractions(projectFileStorageService);
    }

    @Test
    @DisplayName("DIRECTORY 타입은 파일 저장할 수 없다")
    void saveFiles_directoryType() {
        // given
        Long projectId = 1L;
        Long userId = 1L;
        Long projectFileId = 10L;

        Project project = mock(Project.class);

        given(project.getId())
                .willReturn(projectId);

        given(projectRepository.findByIdAndStatus(
                projectId,
                ProjectStatus.ACTIVE
        )).willReturn(Optional.of(project));

        User user = createUser(userId);

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        ProjectFile directory = createDirectory(
                project,
                null,
                projectFileId,
                "src",
                "/src"
        );

        given(projectFileRepository.findById(projectFileId))
                .willReturn(Optional.of(directory));

        ProjectFileSaveRequest request = new ProjectFileSaveRequest(
                userId,
                null,
                List.of(
                        new ProjectFileSaveItemRequest(
                                projectFileId,
                                "content"
                        )
                )
        );

        // when & then
        assertThatThrownBy(() -> projectFileService.saveFiles(projectId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("디렉토리는 저장할 수 없습니다.");

        verifyNoInteractions(projectSaveBatchRepository);
        verifyNoInteractions(fileVersionRepository);
        verifyNoInteractions(projectFileStorageService);
    }

    private User createUser(Long id) {
        User user = User.builder()
                .email("user" + id + "@test.com")
                .nickname("테스터" + id)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        ReflectionTestUtils.setField(user, "id", id);

        return user;
    }

    private GuestSession createGuestSession(Long id) {
        GuestSession guestSession = GuestSession.builder()
                .guestToken("guest-token-" + id)
                .clientIp("127.0.0.1")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        ReflectionTestUtils.setField(guestSession, "id", id);

        return guestSession;
    }
}
