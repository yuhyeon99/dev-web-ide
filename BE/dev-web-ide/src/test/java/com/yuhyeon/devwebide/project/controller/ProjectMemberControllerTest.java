package com.yuhyeon.devwebide.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuhyeon.devwebide.project.domain.ProjectMemberRole;
import com.yuhyeon.devwebide.project.domain.ProjectMemberStatus;
import com.yuhyeon.devwebide.project.dto.ProjectMemberInviteRequest;
import com.yuhyeon.devwebide.project.dto.ProjectMemberManageResponse;
import com.yuhyeon.devwebide.project.dto.ProjectMemberRoleUpdateRequest;
import com.yuhyeon.devwebide.project.service.ProjectMemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectMemberController.class)
class ProjectMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProjectMemberService projectMemberService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("프로젝트 멤버 초대 API 요청에 성공한다")
    void inviteMember() throws Exception {
        Long projectId = 1L;
        Long requesterUserId = 10L;
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
                eq(requesterUserId),
                any(ProjectMemberInviteRequest.class)
        )).willReturn(response);

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .header("X-User-Id", requesterUserId)
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
                .inviteMember(eq(projectId), eq(requesterUserId), any(ProjectMemberInviteRequest.class));
    }

    @Test
    @DisplayName("프로젝트 멤버 권한 변경 API 요청에 성공한다")
    void updateMemberRole() throws Exception {
        Long projectId = 1L;
        Long memberId = 2L;
        Long requesterUserId = 10L;
        ProjectMemberRoleUpdateRequest request =
                new ProjectMemberRoleUpdateRequest(ProjectMemberRole.VIEWER);
        ProjectMemberManageResponse response = createResponse(
                ProjectMemberRole.VIEWER,
                ProjectMemberStatus.ACTIVE
        );

        given(projectMemberService.updateMemberRole(
                eq(projectId),
                eq(memberId),
                eq(requesterUserId),
                any(ProjectMemberRoleUpdateRequest.class)
        )).willReturn(response);

        mockMvc.perform(patch("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                        .header("X-User-Id", requesterUserId)
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
                        eq(requesterUserId),
                        any(ProjectMemberRoleUpdateRequest.class)
                );
    }

    @Test
    @DisplayName("프로젝트 멤버 제거 API 요청에 성공한다")
    void removeMember() throws Exception {
        Long projectId = 1L;
        Long memberId = 2L;
        Long requesterUserId = 10L;
        ProjectMemberManageResponse response = createResponse(
                ProjectMemberRole.EDITOR,
                ProjectMemberStatus.REMOVED
        );

        given(projectMemberService.removeMember(projectId, memberId, requesterUserId))
                .willReturn(response);

        mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}", projectId, memberId)
                        .header("X-User-Id", requesterUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectMemberId").value(1L))
                .andExpect(jsonPath("$.userId").value(20L))
                .andExpect(jsonPath("$.nickname").value("member"))
                .andExpect(jsonPath("$.role").value("EDITOR"))
                .andExpect(jsonPath("$.status").value("REMOVED"));

        then(projectMemberService).should()
                .removeMember(projectId, memberId, requesterUserId);
    }

    @Test
    @DisplayName("멤버 초대 요청에서 userId가 없으면 400 Bad Request를 반환한다")
    void inviteMemberWithoutUserId() throws Exception {
        ProjectMemberInviteRequest request = new ProjectMemberInviteRequest(
                null,
                ProjectMemberRole.EDITOR
        );

        mockMvc.perform(post("/api/projects/{projectId}/members", 1L)
                        .header("X-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectMemberService);
    }

    @Test
    @DisplayName("권한 변경 요청에서 role이 없으면 400 Bad Request를 반환한다")
    void updateMemberRoleWithoutRole() throws Exception {
        ProjectMemberRoleUpdateRequest request =
                new ProjectMemberRoleUpdateRequest(null);

        mockMvc.perform(patch("/api/projects/{projectId}/members/{memberId}", 1L, 2L)
                        .header("X-User-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

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
}
