package com.yuhyeon.devwebide.project.service;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectFile;
import com.yuhyeon.devwebide.project.domain.ProjectFileStatus;
import com.yuhyeon.devwebide.project.domain.ProjectFileType;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.dto.ProjectFileTreeResponse;
import com.yuhyeon.devwebide.project.repository.ProjectFileRepository;
import com.yuhyeon.devwebide.project.repository.ProjectRepository;
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

@ExtendWith(MockitoExtension.class)
class ProjectFileServiceTest {

    @InjectMocks
    private ProjectFileService projectFileService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectFileRepository projectFileRepository;

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
        ProjectFile projectFile = ProjectFile.builder()
                .project(project)
                .parentFile(parentFile)
                .name(name)
                .path(path)
                .fileType(ProjectFileType.FILE)
                .mimeType(mimeType)
                .sizeBytes(sizeBytes)
                .status(ProjectFileStatus.ACTIVE)
                .build();

        ReflectionTestUtils.setField(projectFile, "id", id);

        return projectFile;
    }
}