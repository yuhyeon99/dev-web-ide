package com.yuhyeon.devwebide.execution.controller;

import com.yuhyeon.devwebide.execution.domain.ContainerInstanceStatus;
import com.yuhyeon.devwebide.execution.dto.ContainerInstanceResponse;
import com.yuhyeon.devwebide.execution.dto.ProjectRunRequest;
import com.yuhyeon.devwebide.execution.dto.ProjectRunResponse;
import com.yuhyeon.devwebide.execution.service.ProjectRunService;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProjectRunController 테스트
 *
 * POST /api/projects/{projectId}/run API 정상 응답을 검증합니다.
 */
@WebMvcTest(ProjectRunController.class)
class ProjectRunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectRunService projectRunService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("회원 사용자는 프로젝트를 실행할 수 있다")
    void runProjectByUser() throws Exception {
        Long projectId = 1L;
        ProjectRunRequest request = new ProjectRunRequest(10L, null);
        ProjectRunResponse response = createProjectRunResponse(projectId);

        given(projectRunService.runProject(eq(projectId), refEq(request)))
                .willReturn(response);

        mockMvc.perform(post("/api/projects/{projectId}/run", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.workspaceSessionId").value(100L))
                .andExpect(jsonPath("$.containerInstanceId").value(200L))
                .andExpect(jsonPath("$.runtimeName").value("nodejs"))
                .andExpect(jsonPath("$.runtimeDisplayName").value("Node.js"))
                .andExpect(jsonPath("$.runtimeLanguage").value("NODE"))
                .andExpect(jsonPath("$.dockerImage").value("node:20"))
                .andExpect(jsonPath("$.workspaceStatus").value("STARTING"))
                .andExpect(jsonPath("$.efsMountPath").value("/projects/1"))
                .andExpect(jsonPath("$.container.id").value(200L))
                .andExpect(jsonPath("$.container.provider").value("LOCAL"))
                .andExpect(jsonPath("$.container.taskArn").doesNotExist())
                .andExpect(jsonPath("$.container.containerId").value("local-container-1"))
                .andExpect(jsonPath("$.container.dockerImage").value("node:20"))
                .andExpect(jsonPath("$.container.status").value("RUNNING"))
                .andExpect(jsonPath("$.container.efsMountPath").value("/projects/1"));

        then(projectRunService).should()
                .runProject(eq(projectId), refEq(request));
    }

    @Test
    @DisplayName("게스트 사용자는 프로젝트를 실행할 수 있다")
    void runProjectByGuest() throws Exception {
        Long projectId = 2L;
        ProjectRunRequest request = new ProjectRunRequest(null, 20L);
        ProjectRunResponse response = createGuestProjectRunResponse(projectId);

        given(projectRunService.runProject(eq(projectId), refEq(request)))
                .willReturn(response);

        mockMvc.perform(post("/api/projects/{projectId}/run", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.workspaceSessionId").value(101L))
                .andExpect(jsonPath("$.containerInstanceId").value(201L))
                .andExpect(jsonPath("$.runtimeName").value("python"))
                .andExpect(jsonPath("$.runtimeDisplayName").value("Python"))
                .andExpect(jsonPath("$.runtimeLanguage").value("PYTHON"))
                .andExpect(jsonPath("$.dockerImage").value("python:3.12"))
                .andExpect(jsonPath("$.workspaceStatus").value("STARTING"))
                .andExpect(jsonPath("$.efsMountPath").value("/projects/2"))
                .andExpect(jsonPath("$.container.id").value(201L))
                .andExpect(jsonPath("$.container.provider").value("LOCAL"))
                .andExpect(jsonPath("$.container.taskArn").doesNotExist())
                .andExpect(jsonPath("$.container.containerId").value("local-container-2"))
                .andExpect(jsonPath("$.container.dockerImage").value("python:3.12"))
                .andExpect(jsonPath("$.container.status").value("RUNNING"))
                .andExpect(jsonPath("$.container.efsMountPath").value("/projects/2"));

        then(projectRunService).should()
                .runProject(eq(projectId), refEq(request));
    }

    private ProjectRunResponse createProjectRunResponse(Long projectId) {
        ContainerInstanceResponse container = new ContainerInstanceResponse(
                200L,
                "LOCAL",
                null,
                "local-container-1",
                "node:20",
                ContainerInstanceStatus.RUNNING,
                "/projects/1"
        );

        return new ProjectRunResponse(
                projectId,
                100L,
                200L,
                "nodejs",
                "Node.js",
                RuntimeLanguage.NODE,
                "node:20",
                WorkspaceSessionStatus.STARTING,
                "/projects/1",
                container
        );
    }

    private ProjectRunResponse createGuestProjectRunResponse(Long projectId) {
        ContainerInstanceResponse container = new ContainerInstanceResponse(
                201L,
                "LOCAL",
                null,
                "local-container-2",
                "python:3.12",
                ContainerInstanceStatus.RUNNING,
                "/projects/2"
        );

        return new ProjectRunResponse(
                projectId,
                101L,
                201L,
                "python",
                "Python",
                RuntimeLanguage.PYTHON,
                "python:3.12",
                WorkspaceSessionStatus.STARTING,
                "/projects/2",
                container
        );
    }
}
