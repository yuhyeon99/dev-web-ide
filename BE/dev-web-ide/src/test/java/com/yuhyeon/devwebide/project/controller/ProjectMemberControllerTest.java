package com.yuhyeon.devwebide.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuhyeon.devwebide.auth.dto.AuthenticatedPrincipal;
import com.yuhyeon.devwebide.auth.security.AccessTokenAuthenticationService;
import com.yuhyeon.devwebide.project.domain.ProjectMemberRole;
import com.yuhyeon.devwebide.project.domain.ProjectMemberStatus;
import com.yuhyeon.devwebide.project.dto.ProjectMemberInviteRequest;
import com.yuhyeon.devwebide.project.dto.ProjectMemberManageResponse;
import com.yuhyeon.devwebide.project.dto.ProjectMemberRoleUpdateRequest;
import com.yuhyeon.devwebide.project.service.ProjectMemberService;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProjectMemberService projectMemberService;

    @MockitoBean
    private AccessTokenAuthenticationService accessTokenAuthenticationService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("프로젝트 멤버 초대 API 요청에 성공한다")
    void inviteMember() throws Exception {
        Long projectId = 1L;
        AuthenticatedPrincipal principal = userPrincipal(10L);
        ProjectMemberInviteRequest request = new ProjectMemberInviteRequest(
                20L,
                ProjectMemberRole.EDITOR
        );
        ProjectMemberManageResponse response = createResponse(
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.INVITED
        );

        given(projectMemberService.inviteMember(
                eq(projectId),
                any(ProjectMemberInviteRequest.class),
                eq(principal)
        )).willReturn(response);

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectMemberId").value(1L))
                .andExpect(jsonPath("$.userId").value(20L))
                .andExpect(jsonPath("$.nickname").value("member"))
                .andExpect(jsonPath("$.role").value("EDITOR"))
                .andExpect(jsonPath("$.status").value("INVITED"))
                .andExpect(jsonPath("$.invitedAt").exists())
                .andExpect(jsonPath("$.joinedAt").doesNotExist());

        then(projectMemberService).should()
                .inviteMember(eq(projectId), any(ProjectMemberInviteRequest.class), eq(principal));
    }

    @Test
    @DisplayName("프로젝트 멤버 권한 변경 API 요청에 성공한다")
    void updateMemberRole() throws Exception {
        Long projectId = 1L;
        Long memberId = 2L;
        AuthenticatedPrincipal principal = userPrincipal(10L);
        ProjectMemberRoleUpdateRequest request =
                new ProjectMemberRoleUpdateRequest(ProjectMemberRole.VIEWER);
        ProjectMemberManageResponse response = createResponse(
                ProjectMemberRole.VIEWER,
                ProjectMemberStatus.ACTIVE
        );

        given(projectMemberService.updateMemberRole(
                eq(projectId),
                eq(memberId),
                any(ProjectMemberRoleUpdateRequest.class),
                eq(principal)
        )).willReturn(response);

        mockMvc.perform(patch("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectMemberId").value(1L))
                .andExpect(jsonPath("$.userId").value(20L))
                .andExpect(jsonPath("$.nickname").value("member"))
                .andExpect(jsonPath("$.role").value("VIEWER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.joinedAt").exists());

        then(projectMemberService).should()
                .updateMemberRole(
                        eq(projectId),
                        eq(memberId),
                        any(ProjectMemberRoleUpdateRequest.class),
                        eq(principal)
                );
    }

    @Test
    @DisplayName("프로젝트 멤버 제거 API 요청에 성공한다")
    void removeMember() throws Exception {
        Long projectId = 1L;
        Long memberId = 2L;
        AuthenticatedPrincipal principal = userPrincipal(10L);
        ProjectMemberManageResponse response = createResponse(
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.REMOVED
        );

        given(projectMemberService.removeMember(projectId, memberId, principal))
                .willReturn(response);

        mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                        .with(authentication(authenticationToken(principal))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectMemberId").value(1L))
                .andExpect(jsonPath("$.userId").value(20L))
                .andExpect(jsonPath("$.nickname").value("member"))
                .andExpect(jsonPath("$.role").value("EDITOR"))
                .andExpect(jsonPath("$.status").value("REMOVED"));

        then(projectMemberService).should()
                .removeMember(projectId, memberId, principal);
    }

    @Test
    @DisplayName("프로젝트 멤버 초대 수락 API 요청에 성공한다")
    void acceptInvitation() throws Exception {
        Long projectId = 1L;
        Long memberId = 2L;
        AuthenticatedPrincipal principal = userPrincipal(20L);
        ProjectMemberManageResponse response = createResponse(
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.ACTIVE
        );

        given(projectMemberService.acceptInvitation(projectId, memberId, principal))
                .willReturn(response);

        mockMvc.perform(post("/api/projects/{projectId}/members/{memberId}/accept", projectId, memberId)
                        .with(authentication(authenticationToken(principal))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectMemberId").value(1L))
                .andExpect(jsonPath("$.userId").value(20L))
                .andExpect(jsonPath("$.nickname").value("member"))
                .andExpect(jsonPath("$.role").value("EDITOR"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.invitedAt").exists())
                .andExpect(jsonPath("$.joinedAt").exists());

        then(projectMemberService).should()
                .acceptInvitation(projectId, memberId, principal);
    }

    @Test
    @DisplayName("멤버 초대 요청에서 userId가 없으면 400 Bad Request를 반환한다")
    void inviteMemberWithoutUserId() throws Exception {
        AuthenticatedPrincipal principal = userPrincipal(10L);
        ProjectMemberInviteRequest request = new ProjectMemberInviteRequest(
                null,
                ProjectMemberRole.EDITOR
        );

        mockMvc.perform(post("/api/projects/{projectId}/members", 1L)
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectMemberService);
    }

    @Test
    @DisplayName("권한 변경 요청에서 role이 없으면 400 Bad Request를 반환한다")
    void updateMemberRoleWithoutRole() throws Exception {
        AuthenticatedPrincipal principal = userPrincipal(10L);
        ProjectMemberRoleUpdateRequest request =
                new ProjectMemberRoleUpdateRequest(null);

        mockMvc.perform(patch("/api/projects/{projectId}/members/{memberId}", 1L, 2L)
                        .with(authentication(authenticationToken(principal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectMemberService);
    }

    @Test
    @DisplayName("ProjectMember API는 인증이 없으면 401을 반환한다")
    void projectMemberApisWithoutAuthentication() throws Exception {
        ProjectMemberInviteRequest inviteRequest = new ProjectMemberInviteRequest(
                20L,
                ProjectMemberRole.EDITOR
        );
        ProjectMemberRoleUpdateRequest updateRequest =
                new ProjectMemberRoleUpdateRequest(ProjectMemberRole.VIEWER);

        mockMvc.perform(post("/api/projects/{projectId}/members", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/projects/{projectId}/members/{memberId}/accept", 1L, 2L))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/projects/{projectId}/members/{memberId}", 1L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}", 1L, 2L))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projectMemberService);
    }

    private ProjectMemberManageResponse createResponse(
            ProjectMemberRole role,
            ProjectMemberStatus status
    ) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 8, 10, 0);

        return new ProjectMemberManageResponse(
                1L,
                20L,
                "member",
                role,
                status,
                now,
                status == ProjectMemberStatus.INVITED ? null : now
        );
    }

    private AuthenticatedPrincipal userPrincipal(Long userId) {
        return new AuthenticatedPrincipal(
                100L,
                "USER",
                userId,
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
