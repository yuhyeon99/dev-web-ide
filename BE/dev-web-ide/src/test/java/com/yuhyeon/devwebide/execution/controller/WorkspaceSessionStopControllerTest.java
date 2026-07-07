package com.yuhyeon.devwebide.execution.controller;

import com.yuhyeon.devwebide.execution.domain.ContainerInstanceStatus;
import com.yuhyeon.devwebide.execution.dto.WorkspaceSessionStopResponse;
import com.yuhyeon.devwebide.execution.service.WorkspaceSessionStopService;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSessionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkspaceSessionStopController.class)
class WorkspaceSessionStopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkspaceSessionStopService workspaceSessionStopService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("워크스페이스 세션 실행을 중지한다")
    void stopWorkspaceSession() throws Exception {
        Long workspaceSessionId = 100L;
        WorkspaceSessionStopResponse response = new WorkspaceSessionStopResponse(
                workspaceSessionId,
                200L,
                WorkspaceSessionStatus.STOPPED,
                ContainerInstanceStatus.STOPPED,
                LocalDateTime.of(2026, 7, 7, 10, 0)
        );

        given(workspaceSessionStopService.stopWorkspaceSession(workspaceSessionId))
                .willReturn(response);

        mockMvc.perform(post(
                                "/api/workspace-sessions/{workspaceSessionId}/stop",
                                workspaceSessionId
                        )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.workspaceSessionId").value(workspaceSessionId))
                .andExpect(jsonPath("$.containerInstanceId").value(200L))
                .andExpect(jsonPath("$.workspaceStatus").value("STOPPED"))
                .andExpect(jsonPath("$.containerStatus").value("STOPPED"))
                .andExpect(jsonPath("$.stoppedAt").exists());

        then(workspaceSessionStopService).should()
                .stopWorkspaceSession(workspaceSessionId);
    }
}
