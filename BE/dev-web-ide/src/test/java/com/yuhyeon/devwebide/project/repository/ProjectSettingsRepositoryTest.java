package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectSettings;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProjectSettingsRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RuntimeRepository runtimeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectSettingsRepository projectSettingsRepository;

    @Test
    @DisplayName("프로젝트 설정 저장")
    void saveProjectSettings() {
        Project project = createAndSaveProject(
                "settings-save@test.com",
                "프로젝트설정저장테스터",
                "node-20-settings-save",
                "설정 저장 프로젝트"
        );

        ProjectSettings projectSettings = ProjectSettings.builder()
                .project(project)
                .autoSaveEnabled(false)
                .formatOnSaveEnabled(false)
                .guestCanEdit(false)
                .shareCursorPosition(true)
                .build();

        ProjectSettings savedProjectSettings =
                projectSettingsRepository.save(projectSettings);

        assertThat(savedProjectSettings.getId()).isNotNull();
        assertThat(savedProjectSettings.getProject().getId())
                .isEqualTo(project.getId());
        assertThat(savedProjectSettings.isAutoSaveEnabled()).isFalse();
        assertThat(savedProjectSettings.isFormatOnSaveEnabled()).isFalse();
        assertThat(savedProjectSettings.isGuestCanEdit()).isFalse();
        assertThat(savedProjectSettings.isShareCursorPosition()).isTrue();
        assertThat(savedProjectSettings.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("프로젝트 ID로 프로젝트 설정 조회")
    void findByProjectId() {
        Project project = createAndSaveProject(
                "settings-find@test.com",
                "프로젝트설정조회테스터",
                "node-20-settings-find",
                "설정 조회 프로젝트"
        );

        ProjectSettings projectSettings = ProjectSettings.builder()
                .project(project)
                .autoSaveEnabled(true)
                .formatOnSaveEnabled(true)
                .guestCanEdit(false)
                .shareCursorPosition(true)
                .build();

        projectSettingsRepository.save(projectSettings);

        Optional<ProjectSettings> result =
                projectSettingsRepository.findByProjectId(project.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getProject().getId())
                .isEqualTo(project.getId());
        assertThat(result.get().isAutoSaveEnabled()).isTrue();
        assertThat(result.get().isFormatOnSaveEnabled()).isTrue();
        assertThat(result.get().isGuestCanEdit()).isFalse();
        assertThat(result.get().isShareCursorPosition()).isTrue();
    }

    @Test
    @DisplayName("프로젝트 ID 기준 설정 존재 여부 확인")
    void existsByProjectId() {
        Project project = createAndSaveProject(
                "settings-exists@test.com",
                "프로젝트설정존재테스터",
                "node-20-settings-exists",
                "설정 존재 프로젝트"
        );

        ProjectSettings projectSettings = ProjectSettings.createDefault(project);

        projectSettingsRepository.save(projectSettings);

        boolean exists =
                projectSettingsRepository.existsByProjectId(project.getId());

        boolean notExists =
                projectSettingsRepository.existsByProjectId(999999L);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("프로젝트 기본 설정 생성")
    void createDefaultProjectSettings() {
        Project project = createAndSaveProject(
                "settings-default@test.com",
                "프로젝트설정기본값테스터",
                "node-20-settings-default",
                "기본 설정 프로젝트"
        );

        ProjectSettings projectSettings = ProjectSettings.createDefault(project);

        ProjectSettings savedProjectSettings =
                projectSettingsRepository.save(projectSettings);

        assertThat(savedProjectSettings.getId()).isNotNull();
        assertThat(savedProjectSettings.getProject().getId())
                .isEqualTo(project.getId());

        assertThat(savedProjectSettings.isAutoSaveEnabled()).isFalse();
        assertThat(savedProjectSettings.isFormatOnSaveEnabled()).isFalse();
        assertThat(savedProjectSettings.isGuestCanEdit()).isFalse();
        assertThat(savedProjectSettings.isShareCursorPosition()).isTrue();
    }

    @Test
    @DisplayName("프로젝트 설정 일괄 변경")
    void updateSettings() {
        Project project = createAndSaveProject(
                "settings-update@test.com",
                "프로젝트설정수정테스터",
                "node-20-settings-update",
                "설정 수정 프로젝트"
        );

        ProjectSettings projectSettings =
                ProjectSettings.createDefault(project);

        ProjectSettings savedProjectSettings =
                projectSettingsRepository.save(projectSettings);

        savedProjectSettings.updateSettings(
                true,
                true,
                true,
                false
        );

        ProjectSettings updatedProjectSettings =
                projectSettingsRepository.save(savedProjectSettings);

        assertThat(updatedProjectSettings.isAutoSaveEnabled()).isTrue();
        assertThat(updatedProjectSettings.isFormatOnSaveEnabled()).isTrue();
        assertThat(updatedProjectSettings.isGuestCanEdit()).isTrue();
        assertThat(updatedProjectSettings.isShareCursorPosition()).isFalse();
    }

    private Project createAndSaveProject(
            String email,
            String nickname,
            String runtimeName,
            String projectName
    ) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        Runtime runtime = Runtime.builder()
                .name(runtimeName)
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
                .name(projectName)
                .description("프로젝트 설정 테스트용 프로젝트입니다.")
                .projectType(ProjectType.PERSONAL)
                .visibility(ProjectVisibility.PRIVATE)
                .status(ProjectStatus.ACTIVE)
                .storagePath("/projects/settings-test")
                .build();

        return projectRepository.save(project);
    }
}