package com.yuhyeon.devwebide.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import com.yuhyeon.devwebide.project.domain.ProjectVisibility;
import com.yuhyeon.devwebide.project.dto.ProjectCreateRequest;
import com.yuhyeon.devwebide.project.dto.ProjectCreateResponse;
import com.yuhyeon.devwebide.project.service.ProjectService;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProjectController 테스트
 *
 * POST /api/projects 요청을 검증합니다.
 */
@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProjectService projectService;

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
                eq(1L),
                isNull()
        )).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/projects")
                        .header("X-User-Id", 1L)
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
                eq(1L),
                isNull()
        );
    }

    @Test
    @DisplayName("팀 프로젝트 생성 API 요청에 성공한다")
    void createTeamProject() throws Exception {
        // given
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
                eq(1L),
                isNull()
        )).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/projects")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("team-project"))
                .andExpect(jsonPath("$.projectType").value("TEAM"))
                .andExpect(jsonPath("$.visibility").value("TEAM"));

        verify(projectService).createProject(
                any(ProjectCreateRequest.class),
                eq(1L),
                isNull()
        );
    }

    @Test
    @DisplayName("게스트 프로젝트 생성 API 요청에 성공한다")
    void createGuestProject() throws Exception {
        // given
        ProjectCreateRequest request = new ProjectCreateRequest(
                "guest-project",
                "게스트 프로젝트입니다.",
                1L,
                ProjectType.GUEST,
                ProjectVisibility.PRIVATE,
                List.of()
        );

        ProjectCreateResponse response = new ProjectCreateResponse(
                3L,
                "guest-project",
                "게스트 프로젝트입니다.",
                ProjectType.GUEST,
                ProjectVisibility.PRIVATE,
                ProjectStatus.ACTIVE,
                1L,
                "python-3.12",
                "Python 3.12",
                RuntimeLanguage.values()[0],
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );

        when(projectService.createProject(
                any(ProjectCreateRequest.class),
                isNull(),
                eq(1L)
        )).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/projects")
                        .header("X-Guest-Session-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("guest-project"))
                .andExpect(jsonPath("$.projectType").value("GUEST"))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));

        verify(projectService).createProject(
                any(ProjectCreateRequest.class),
                isNull(),
                eq(1L)
        );
    }

    @Test
    @DisplayName("프로젝트 이름이 비어 있으면 400 Bad Request를 반환한다")
    void createProjectWithBlankName() throws Exception {
        // given
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
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("런타임 ID가 없으면 400 Bad Request를 반환한다")
    void createProjectWithoutRuntimeId() throws Exception {
        // given
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
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("프로젝트 타입이 없으면 400 Bad Request를 반환한다")
    void createProjectWithoutProjectType() throws Exception {
        // given
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
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }

    @Test
    @DisplayName("프로젝트 공개 범위가 없으면 400 Bad Request를 반환한다")
    void createProjectWithoutVisibility() throws Exception {
        // given
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
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }


}