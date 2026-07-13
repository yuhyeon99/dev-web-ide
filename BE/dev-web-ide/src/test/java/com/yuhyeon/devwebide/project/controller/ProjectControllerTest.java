package com.yuhyeon.devwebide.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.auth.security.AccessTokenAuthenticationService;
import com.yuhyeon.devwebide.project.domain.*;
import com.yuhyeon.devwebide.project.dto.*;
import com.yuhyeon.devwebide.project.service.ProjectService;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.runtime.dto.RuntimeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProjectController 테스트
 *
 * POST /api/projects 요청을 검증합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private AccessTokenAuthenticationService accessTokenAuthenticationService;

    /**
     * @WebMvcTest에서는 JPA 관련 Bean이 로딩되지 않기 때문에
     * Auditing 설정에서 필요한 JpaMetamodelMappingContext를 Mock으로 등록합니다.
     */
    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("개인 프로젝트 생성 API 요청에 성공한다")
    void createPersonalProject() throws Exception {
        // given
        AuthenticatedPrincipal principal = userPrincipal();
        ProjectCreateRequest request = new ProjectCreateRequest(
                "personal-project",
                "개인 프로젝트입니다.",
                1L,
                ProjectType.PERSONAL,
                ProjectVisibility.PRIVATE,
                List.of()
        );

        ProjectCreateResponse response = new ProjectCreateResponse(
                1L,
                "personal-project",
                "개인 프로젝트입니다.",
                ProjectType.PERSONAL,
                ProjectVisibility.PRIVATE,
                ProjectStatus.ACTIVE,
                1L,
                "node-20",
                "Node.js 20",
                RuntimeLanguage.values()[0],
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        when(projectService.createProject(
                any(ProjectCreateRequest.class),
                org.mockito.ArgumentMatchers.eq(principal)
        )).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/projects")
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("personal-project"))
                .andExpect(jsonPath("$.description").value("개인 프로젝트입니다."))
                .andExpect(jsonPath("$.projectType").value("PERSONAL"))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.runtimeId").value(1L))
                .andExpect(jsonPath("$.runtimeName").value("node-20"))
                .andExpect(jsonPath("$.runtimeDisplayName").value("Node.js 20"));

        verify(projectService).createProject(
                any(ProjectCreateRequest.class),
                org.mockito.ArgumentMatchers.eq(principal)
        );
    }

    @Test
    @DisplayName("팀 프로젝트 생성 API 요청에 성공한다")
    void createTeamProject() throws Exception {
        // given
        AuthenticatedPrincipal principal = userPrincipal();
        ProjectCreateRequest request = new ProjectCreateRequest(
                "team-project",
                "팀 프로젝트입니다.",
                1L,
                ProjectType.TEAM,
                ProjectVisibility.TEAM,
                List.of(2L, 3L)
        );

        ProjectCreateResponse response = new ProjectCreateResponse(
                2L,
                "team-project",
                "팀 프로젝트입니다.",
                ProjectType.TEAM,
                ProjectVisibility.TEAM,
                ProjectStatus.ACTIVE,
                1L,
                "java-21",
                "Java 21",
                RuntimeLanguage.values()[0],
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        when(projectService.createProject(
                any(ProjectCreateRequest.class),
                org.mockito.ArgumentMatchers.eq(principal)
        )).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/projects")
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("team-project"))
                .andExpect(jsonPath("$.projectType").value("TEAM"))
                .andExpect(jsonPath("$.visibility").value("TEAM"));

        verify(projectService).createProject(
                any(ProjectCreateRequest.class),
                org.mockito.ArgumentMatchers.eq(principal)
        );
    }

    @Test
    @DisplayName("프로젝트 생성 API는 인증이 없으면 401을 반환한다")
    void createProjectWithoutAuthentication() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "personal-project",
                "개인 프로젝트입니다.",
                1L,
                ProjectType.PERSONAL,
                ProjectVisibility.PRIVATE,
                List.of()
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("프로젝트 이름이 비어 있으면 400 Bad Request를 반환한다")
    void createProjectWithBlankName() throws Exception {
        // given
        AuthenticatedPrincipal principal = userPrincipal();
        ProjectCreateRequest request = new ProjectCreateRequest(
                "",
                "잘못된 요청입니다.",
                1L,
                ProjectType.PERSONAL,
                ProjectVisibility.PRIVATE,
                List.of()
        );

        // when & then
        mockMvc.perform(post("/api/projects")
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("런타임 ID가 없으면 400 Bad Request를 반환한다")
    void createProjectWithoutRuntimeId() throws Exception {
        // given
        AuthenticatedPrincipal principal = userPrincipal();
        ProjectCreateRequest request = new ProjectCreateRequest(
                "invalid-project",
                "런타임 ID가 없는 요청입니다.",
                null,
                ProjectType.PERSONAL,
                ProjectVisibility.PRIVATE,
                List.of()
        );

        // when & then
        mockMvc.perform(post("/api/projects")
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("프로젝트 타입이 없으면 400 Bad Request를 반환한다")
    void createProjectWithoutProjectType() throws Exception {
        // given
        AuthenticatedPrincipal principal = userPrincipal();
        ProjectCreateRequest request = new ProjectCreateRequest(
                "invalid-project",
                "프로젝트 타입이 없는 요청입니다.",
                1L,
                null,
                ProjectVisibility.PRIVATE,
                List.of()
        );

        // when & then
        mockMvc.perform(post("/api/projects")
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("프로젝트 공개 범위가 없으면 400 Bad Request를 반환한다")
    void createProjectWithoutVisibility() throws Exception {
        // given
        AuthenticatedPrincipal principal = userPrincipal();
        ProjectCreateRequest request = new ProjectCreateRequest(
                "invalid-project",
                "공개 범위가 없는 요청입니다.",
                1L,
                ProjectType.PERSONAL,
                null,
                List.of()
        );

        // when & then
        mockMvc.perform(post("/api/projects")
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("내 프로젝트 목록 조회 API 요청에 성공한다")
    void getMyProjects() throws Exception {
        // given
        AuthenticatedPrincipal principal = userPrincipal();

        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);

        ProjectSummaryResponse firstProject = new ProjectSummaryResponse(
                1L,
                "personal-project",
                "개인 프로젝트입니다.",
                ProjectType.PERSONAL,
                ProjectVisibility.PRIVATE,
                ProjectStatus.ACTIVE,
                1L,
                "node-20",
                "Node.js 20",
                RuntimeLanguage.values()[0],
                now,
                now
        );

        ProjectSummaryResponse secondProject = new ProjectSummaryResponse(
                2L,
                "team-project",
                "팀 프로젝트입니다.",
                ProjectType.TEAM,
                ProjectVisibility.TEAM,
                ProjectStatus.ACTIVE,
                2L,
                "java-21",
                "Java 21",
                RuntimeLanguage.values()[0],
                now.minusDays(1),
                now.minusDays(1)
        );

        when(projectService.getMyProjects(principal))
                .thenReturn(List.of(firstProject, secondProject));

        // when & then
        mockMvc.perform(get("/api/projects/my")
                        .with(authentication(authenticationToken(principal))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("personal-project"))
                .andExpect(jsonPath("$[0].description").value("개인 프로젝트입니다."))
                .andExpect(jsonPath("$[0].projectType").value("PERSONAL"))
                .andExpect(jsonPath("$[0].visibility").value("PRIVATE"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].runtimeId").value(1L))
                .andExpect(jsonPath("$[0].runtimeName").value("node-20"))
                .andExpect(jsonPath("$[0].runtimeDisplayName").value("Node.js 20"))

                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("team-project"))
                .andExpect(jsonPath("$[1].description").value("팀 프로젝트입니다."))
                .andExpect(jsonPath("$[1].projectType").value("TEAM"))
                .andExpect(jsonPath("$[1].visibility").value("TEAM"))
                .andExpect(jsonPath("$[1].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].runtimeId").value(2L))
                .andExpect(jsonPath("$[1].runtimeName").value("java-21"))
                .andExpect(jsonPath("$[1].runtimeDisplayName").value("Java 21"));

        verify(projectService).getMyProjects(principal);
    }

    @Test
    @DisplayName("내 프로젝트 목록 조회 API는 인증이 없으면 401을 반환한다")
    void getMyProjectsWithoutAuthentication() throws Exception {
        // when & then
        mockMvc.perform(get("/api/projects/my"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("공유 프로젝트 목록 조회 API 요청에 성공한다")
    void getSharedProjects() throws Exception {
        AuthenticatedPrincipal principal = userPrincipal();
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);
        ProjectSummaryResponse sharedProject = new ProjectSummaryResponse(
                3L,
                "shared-team-project",
                "공유받은 팀 프로젝트입니다.",
                ProjectType.TEAM,
                ProjectVisibility.TEAM,
                ProjectStatus.ACTIVE,
                1L,
                "node-20",
                "Node.js 20",
                RuntimeLanguage.NODE,
                now,
                now
        );

        when(projectService.getSharedProjects(principal))
                .thenReturn(List.of(sharedProject));

        mockMvc.perform(get("/api/projects/shared")
                        .with(authentication(authenticationToken(principal))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3L))
                .andExpect(jsonPath("$[0].name").value("shared-team-project"))
                .andExpect(jsonPath("$[0].projectType").value("TEAM"));

        verify(projectService).getSharedProjects(principal);
    }

    @Test
    @DisplayName("프로젝트 상세 조회")
    void getProjectDetail() throws Exception {
        AuthenticatedPrincipal principal = userPrincipal();
        Long projectId = 1L;

        RuntimeResponse runtime = new RuntimeResponse(
                1L,
                "node-20",
                "Node.js 20",
                "20",
                "node:20",
                RuntimeLanguage.NODE
        );

        ProjectSettingsResponse settings = new ProjectSettingsResponse(
                false,
                false,
                false,
                true
        );

        ProjectMemberResponse member = new ProjectMemberResponse(
                1L,
                "테스터",
                ProjectMemberRole.OWNER,
                ProjectMemberStatus.ACTIVE,
                LocalDateTime.of(2026, 6, 29, 10, 10)
        );

        ProjectDetailResponse response = new ProjectDetailResponse(
                projectId,
                "my-project",
                "테스트 프로젝트",
                ProjectType.PERSONAL,
                ProjectVisibility.PRIVATE,
                ProjectStatus.ACTIVE,
                "/projects/1",
                runtime,
                settings,
                List.of(member),
                LocalDateTime.of(2026, 6, 29, 10, 0),
                LocalDateTime.of(2026, 6, 29, 10, 30)
        );

        given(projectService.getProjectDetail(projectId, principal))
                .willReturn(response);

        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                        .with(authentication(authenticationToken(principal))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("my-project"))
                .andExpect(jsonPath("$.description").value("테스트 프로젝트"))
                .andExpect(jsonPath("$.projectType").value("PERSONAL"))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.storagePath").value("/projects/1"))
                .andExpect(jsonPath("$.runtime.id").value(1L))
                .andExpect(jsonPath("$.runtime.name").value("node-20"))
                .andExpect(jsonPath("$.runtime.displayName").value("Node.js 20"))
                .andExpect(jsonPath("$.runtime.version").value("20"))
                .andExpect(jsonPath("$.runtime.dockerImage").value("node:20"))
                .andExpect(jsonPath("$.runtime.language").value("NODE"))
                .andExpect(jsonPath("$.settings.autoSaveEnabled").value(false))
                .andExpect(jsonPath("$.settings.formatOnSaveEnabled").value(false))
                .andExpect(jsonPath("$.settings.guestCanEdit").value(false))
                .andExpect(jsonPath("$.settings.shareCursorPosition").value(true))
                .andExpect(jsonPath("$.members[0].userId").value(1L))
                .andExpect(jsonPath("$.members[0].nickname").value("테스터"))
                .andExpect(jsonPath("$.members[0].role").value("OWNER"))
                .andExpect(jsonPath("$.members[0].status").value("ACTIVE"));

        then(projectService)
                .should()
                .getProjectDetail(projectId, principal);
    }

    @Test
    @DisplayName("프로젝트 상세 조회 API는 인증이 없으면 401을 반환한다")
    void getProjectDetailWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}", 1L))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("회원 사용자는 프로젝트를 열 수 있다")
    void openProjectByUser() throws Exception {
        // given
        AuthenticatedPrincipal principal = userPrincipal();
        Long projectId = 1L;
        LocalDateTime openedAt = LocalDateTime.of(2026, 1, 1, 10, 30);

        ProjectOpenResponse response = new ProjectOpenResponse(
                projectId,
                "테스트 프로젝트",
                "테스트 프로젝트 설명",
                ProjectType.PERSONAL,
                ProjectVisibility.PRIVATE,
                ProjectStatus.ACTIVE,
                1L,
                "nodejs",
                "Node.js",
                RuntimeLanguage.JAVA,
                openedAt
        );

        when(projectService.openProject(projectId, principal))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/projects/{projectId}/open", projectId)
                        .with(authentication(authenticationToken(principal))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.name").value("테스트 프로젝트"))
                .andExpect(jsonPath("$.description").value("테스트 프로젝트 설명"))
                .andExpect(jsonPath("$.projectType").value("PERSONAL"))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.runtimeId").value(1L))
                .andExpect(jsonPath("$.runtimeName").value("nodejs"))
                .andExpect(jsonPath("$.runtimeDisplayName").value("Node.js"))
                .andExpect(jsonPath("$.runtimeLanguage").value("JAVA"))
                .andExpect(jsonPath("$.openedAt").exists());

        verify(projectService).openProject(projectId, principal);
    }

    @Test
    @DisplayName("프로젝트 열기 API는 인증이 없으면 401을 반환한다")
    void openProjectWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/projects/{projectId}/open", 1L))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("프로젝트 수정 API 요청에 성공한다")
    void updateProject() throws Exception {
        AuthenticatedPrincipal principal = userPrincipal();
        Long projectId = 1L;
        ProjectUpdateRequest request = new ProjectUpdateRequest(
                "updated-project",
                "updated description"
        );

        RuntimeResponse runtime = new RuntimeResponse(
                1L,
                "node-20",
                "Node.js 20",
                "20",
                "node:20",
                RuntimeLanguage.NODE
        );

        ProjectSettingsResponse settings = new ProjectSettingsResponse(
                true,
                false,
                false,
                true
        );

        ProjectMemberResponse member = new ProjectMemberResponse(
                1L,
                "owner",
                ProjectMemberRole.OWNER,
                ProjectMemberStatus.ACTIVE,
                LocalDateTime.of(2026, 7, 8, 10, 0)
        );

        ProjectDetailResponse response = new ProjectDetailResponse(
                projectId,
                "updated-project",
                "updated description",
                ProjectType.PERSONAL,
                ProjectVisibility.PRIVATE,
                ProjectStatus.ACTIVE,
                "/projects/1",
                runtime,
                settings,
                List.of(member),
                LocalDateTime.of(2026, 7, 8, 9, 0),
                LocalDateTime.of(2026, 7, 8, 10, 30)
        );

        given(projectService.updateProject(
                eq(projectId),
                any(ProjectUpdateRequest.class),
                eq(principal)
        )).willReturn(response);

        mockMvc.perform(patch("/api/projects/{projectId}", projectId)
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("updated-project"))
                .andExpect(jsonPath("$.description").value("updated description"))
                .andExpect(jsonPath("$.projectType").value("PERSONAL"))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.storagePath").value("/projects/1"))
                .andExpect(jsonPath("$.runtime.id").value(1L))
                .andExpect(jsonPath("$.runtime.name").value("node-20"))
                .andExpect(jsonPath("$.settings.autoSaveEnabled").value(true))
                .andExpect(jsonPath("$.members[0].userId").value(1L))
                .andExpect(jsonPath("$.members[0].nickname").value("owner"))
                .andExpect(jsonPath("$.members[0].role").value("OWNER"))
                .andExpect(jsonPath("$.members[0].status").value("ACTIVE"));

        then(projectService)
                .should()
                .updateProject(eq(projectId), any(ProjectUpdateRequest.class), eq(principal));
    }

    @Test
    @DisplayName("프로젝트 수정 API는 인증이 없으면 401을 반환한다")
    void updateProjectWithoutAuthentication() throws Exception {
        ProjectUpdateRequest request = new ProjectUpdateRequest(
                "updated-project",
                "updated description"
        );

        mockMvc.perform(patch("/api/projects/{projectId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("프로젝트 수정 요청에서 이름이 비어 있으면 400 Bad Request를 반환한다")
    void updateProjectWithBlankName() throws Exception {
        AuthenticatedPrincipal principal = userPrincipal();
        ProjectUpdateRequest request = new ProjectUpdateRequest(
                "",
                "updated description"
        );

        mockMvc.perform(patch("/api/projects/{projectId}", 1L)
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("프로젝트 수정 요청에서 이름이 200자를 초과하면 400 Bad Request를 반환한다")
    void updateProjectWithTooLongName() throws Exception {
        AuthenticatedPrincipal principal = userPrincipal();
        ProjectUpdateRequest request = new ProjectUpdateRequest(
                "a".repeat(201),
                "updated description"
        );

        mockMvc.perform(patch("/api/projects/{projectId}", 1L)
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("프로젝트 수정 요청에서 설명이 500자를 초과하면 400 Bad Request를 반환한다")
    void updateProjectWithTooLongDescription() throws Exception {
        AuthenticatedPrincipal principal = userPrincipal();
        ProjectUpdateRequest request = new ProjectUpdateRequest(
                "updated-project",
                "a".repeat(501)
        );

        mockMvc.perform(patch("/api/projects/{projectId}", 1L)
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("프로젝트 삭제 API 요청에 성공한다")
    void deleteProject() throws Exception {
        AuthenticatedPrincipal principal = userPrincipal();
        Long projectId = 1L;
        ProjectDeleteResponse response = new ProjectDeleteResponse(
                projectId,
                ProjectStatus.DELETED,
                true,
                LocalDateTime.of(2026, 7, 8, 10, 30)
        );

        given(projectService.deleteProject(projectId, principal))
                .willReturn(response);

        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                        .with(authentication(authenticationToken(principal))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.status").value("DELETED"))
                .andExpect(jsonPath("$.deleted").value(true))
                .andExpect(jsonPath("$.updatedAt").exists());

        then(projectService)
                .should()
                .deleteProject(projectId, principal);
    }

    @Test
    @DisplayName("프로젝트 삭제 API는 인증이 없으면 401을 반환한다")
    void deleteProjectWithoutAuthentication() throws Exception {
        mockMvc.perform(delete("/api/projects/{projectId}", 1L))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projectService);
    }

    private AuthenticatedPrincipal userPrincipal() {
        return new AuthenticatedPrincipal(
                100L,
                "USER",
                1L,
                null,
                "USER"
        );
    }

    private UsernamePasswordAuthenticationToken authenticationToken(
            AuthenticatedPrincipal principal
    ) {
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
