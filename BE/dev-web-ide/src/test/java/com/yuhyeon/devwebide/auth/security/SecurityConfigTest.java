package com.yuhyeon.devwebide.auth.security;

import com.yuhyeon.devwebide.auth.dto.AuthTokenRefreshResponse;
import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.auth.service.AuthService;
import com.yuhyeon.devwebide.auth.service.GuestSessionService;
import com.yuhyeon.devwebide.auth.service.OAuthLoginService;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import com.yuhyeon.devwebide.project.dto.ProjectCreateResponse;
import com.yuhyeon.devwebide.project.dto.ProjectDeleteResponse;
import com.yuhyeon.devwebide.project.dto.ProjectDetailResponse;
import com.yuhyeon.devwebide.project.dto.ProjectFileTreeResponse;
import com.yuhyeon.devwebide.project.dto.ProjectMemberInviteRequest;
import com.yuhyeon.devwebide.project.dto.ProjectMemberManageResponse;
import com.yuhyeon.devwebide.project.dto.ProjectMemberResponse;
import com.yuhyeon.devwebide.project.dto.ProjectMemberRoleUpdateRequest;
import com.yuhyeon.devwebide.project.dto.ProjectOpenResponse;
import com.yuhyeon.devwebide.project.dto.ProjectSettingsResponse;
import com.yuhyeon.devwebide.project.dto.ProjectSummaryResponse;
import com.yuhyeon.devwebide.project.service.ProjectFileService;
import com.yuhyeon.devwebide.project.service.ProjectMemberService;
import com.yuhyeon.devwebide.project.service.ProjectService;
import com.yuhyeon.devwebide.project.domain.ProjectMemberRole;
import com.yuhyeon.devwebide.project.domain.ProjectMemberStatus;
import com.yuhyeon.devwebide.runtime.service.RuntimeService;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.runtime.dto.RuntimeResponse;
import com.yuhyeon.devwebide.user.domain.UserRole;
import com.yuhyeon.devwebide.user.domain.UserStatus;
import com.yuhyeon.devwebide.user.dto.UserMeResponse;
import com.yuhyeon.devwebide.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccessTokenAuthenticationService accessTokenAuthenticationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private OAuthLoginService oAuthLoginService;

    @MockitoBean
    private GuestSessionService guestSessionService;

    @MockitoBean
    private RuntimeService runtimeService;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProjectMemberService projectMemberService;

    @MockitoBean
    private ProjectFileService projectFileService;

    @Test
    @DisplayName("permitAll 경로는 인증 없이 접근할 수 있다")
    void permitAllPath() throws Exception {
        given(authService.refreshAccessToken(null))
                .willReturn(AuthTokenRefreshResponse.of(
                        "access-token",
                        1800L,
                        LocalDateTime.of(2026, 7, 9, 10, 30)
                ));

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/runtimes는 인증 없이 접근할 수 있다")
    void runtimesPermitAll() throws Exception {
        given(runtimeService.getActiveRuntimes())
                .willReturn(List.of());

        mockMvc.perform(get("/api/runtimes"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/users/me는 인증이 없으면 401을 반환한다")
    void usersMeWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 Bearer token이면 GET /api/users/me에 접근할 수 있다")
    void usersMeWithValidBearerToken() throws Exception {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                100L,
                "USER",
                1L,
                null,
                "USER"
        );

        given(accessTokenAuthenticationService.authenticate("access-token"))
                .willReturn(principal);
        given(userService.getCurrentUser(principal))
                .willReturn(new UserMeResponse(
                        1L,
                        "user@test.com",
                        "user",
                        UserRole.USER,
                        UserStatus.ACTIVE
                ));

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Project 기본 API는 인증이 없으면 401을 반환한다")
    void projectBasicApisWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "project",
                                  "description": "description",
                                  "runtimeId": 1,
                                  "projectType": "PERSONAL",
                                  "visibility": "PRIVATE",
                                  "memberUserIds": []
                                }
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/projects/my"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/projects/1")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "updated",
                                  "description": "description"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/projects/1/open"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 Bearer token이면 Project 기본 API에 접근할 수 있다")
    void projectBasicApisWithValidBearerToken() throws Exception {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                100L,
                "USER",
                1L,
                null,
                "USER"
        );

        given(accessTokenAuthenticationService.authenticate("project-access-token"))
                .willReturn(principal);
        given(projectService.createProject(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(principal)
        )).willReturn(new ProjectCreateResponse(
                1L,
                "project",
                "description",
                ProjectType.PERSONAL,
                ProjectVisibility.PRIVATE,
                ProjectStatus.ACTIVE,
                1L,
                "node-20",
                "Node.js 20",
                RuntimeLanguage.NODE,
                LocalDateTime.of(2026, 7, 9, 10, 0)
        ));
        given(projectService.getMyProjects(principal))
                .willReturn(List.of(new ProjectSummaryResponse(
                        1L,
                        "project",
                        "description",
                        ProjectType.PERSONAL,
                        ProjectVisibility.PRIVATE,
                        ProjectStatus.ACTIVE,
                        1L,
                        "node-20",
                        "Node.js 20",
                        RuntimeLanguage.NODE,
                        LocalDateTime.of(2026, 7, 9, 10, 0),
                        LocalDateTime.of(2026, 7, 9, 10, 0)
                )));
        given(projectService.getProjectDetail(1L, principal))
                .willReturn(new ProjectDetailResponse(
                        1L,
                        "project",
                        "description",
                        ProjectType.PERSONAL,
                        ProjectVisibility.PRIVATE,
                        ProjectStatus.ACTIVE,
                        "/projects/1",
                        new RuntimeResponse(
                                1L,
                                "node-20",
                                "Node.js 20",
                                "20",
                                "node:20",
                                RuntimeLanguage.NODE
                        ),
                        new ProjectSettingsResponse(
                                true,
                                false,
                                false,
                                true
                        ),
                        List.of(new ProjectMemberResponse(
                                1L,
                                "user",
                                ProjectMemberRole.OWNER,
                                ProjectMemberStatus.ACTIVE,
                                LocalDateTime.of(2026, 7, 9, 10, 0)
                        )),
                        LocalDateTime.of(2026, 7, 9, 10, 0),
                        LocalDateTime.of(2026, 7, 9, 10, 0)
                ));
        given(projectService.updateProject(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(principal)
        )).willReturn(new ProjectDetailResponse(
                1L,
                "updated",
                "description",
                ProjectType.PERSONAL,
                ProjectVisibility.PRIVATE,
                ProjectStatus.ACTIVE,
                "/projects/1",
                new RuntimeResponse(
                        1L,
                        "node-20",
                        "Node.js 20",
                        "20",
                        "node:20",
                        RuntimeLanguage.NODE
                ),
                new ProjectSettingsResponse(
                        true,
                        false,
                        false,
                        true
                ),
                List.of(),
                LocalDateTime.of(2026, 7, 9, 10, 0),
                LocalDateTime.of(2026, 7, 9, 10, 0)
        ));
        given(projectService.deleteProject(1L, principal))
                .willReturn(new ProjectDeleteResponse(
                        1L,
                        ProjectStatus.DELETED,
                        true,
                        LocalDateTime.of(2026, 7, 9, 10, 0)
                ));
        given(projectService.openProject(1L, principal))
                .willReturn(new ProjectOpenResponse(
                        1L,
                        "project",
                        "description",
                        ProjectType.PERSONAL,
                        ProjectVisibility.PRIVATE,
                        ProjectStatus.ACTIVE,
                        1L,
                        "node-20",
                        "Node.js 20",
                        RuntimeLanguage.NODE,
                        LocalDateTime.of(2026, 7, 9, 10, 0)
                ));

        mockMvc.perform(post("/api/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer project-access-token")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "project",
                                  "description": "description",
                                  "runtimeId": 1,
                                  "projectType": "PERSONAL",
                                  "visibility": "PRIVATE",
                                  "memberUserIds": []
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/projects/my")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer project-access-token"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/projects/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer project-access-token"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/projects/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer project-access-token")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "updated",
                                  "description": "description"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/projects/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer project-access-token"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/projects/1/open")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer project-access-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ProjectMember API는 인증이 없으면 401을 반환한다")
    void projectMemberApisWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/projects/1/members")
                        .contentType("application/json")
                        .content("""
                                {
                                  "userId": 2,
                                  "role": "EDITOR"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/projects/1/members/2/accept"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/projects/1/members/2")
                        .contentType("application/json")
                        .content("""
                                {
                                  "role": "VIEWER"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/projects/1/members/2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 Bearer token이면 ProjectMember API에 접근할 수 있다")
    void projectMemberApisWithValidBearerToken() throws Exception {
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                100L,
                "USER",
                1L,
                null,
                "USER"
        );
        ProjectMemberManageResponse response = new ProjectMemberManageResponse(
                1L,
                2L,
                "member",
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.INVITED,
                LocalDateTime.of(2026, 7, 9, 10, 0),
                null
        );

        given(accessTokenAuthenticationService.authenticate("project-member-token"))
                .willReturn(principal);
        given(projectMemberService.inviteMember(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(ProjectMemberInviteRequest.class),
                org.mockito.ArgumentMatchers.eq(principal)
        )).willReturn(response);
        given(projectMemberService.acceptInvitation(1L, 2L, principal))
                .willReturn(response);
        given(projectMemberService.updateMemberRole(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.any(ProjectMemberRoleUpdateRequest.class),
                org.mockito.ArgumentMatchers.eq(principal)
        )).willReturn(response);
        given(projectMemberService.removeMember(1L, 2L, principal))
                .willReturn(response);

        mockMvc.perform(post("/api/projects/1/members")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer project-member-token")
                        .contentType("application/json")
                        .content("""
                                {
                                  "userId": 2,
                                  "role": "EDITOR"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/projects/1/members/2/accept")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer project-member-token"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/projects/1/members/2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer project-member-token")
                        .contentType("application/json")
                        .content("""
                                {
                                  "role": "VIEWER"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/projects/1/members/2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer project-member-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이번 범위에서 제외한 ProjectFile API는 permitAll을 유지한다")
    void excludedProjectFileApiPermitAll() throws Exception {
        given(projectFileService.getFileTree(1L))
                .willReturn(List.<ProjectFileTreeResponse>of());

        mockMvc.perform(get("/api/projects/1/files/tree"))
                .andExpect(status().isOk());
    }
}
