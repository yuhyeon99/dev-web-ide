package com.yuhyeon.devwebide.terminal.controller;

import com.yuhyeon.devwebide.terminal.domain.TerminalStreamType;
import com.yuhyeon.devwebide.terminal.dto.TerminalLogListResponse;
import com.yuhyeon.devwebide.terminal.dto.TerminalLogResponse;
import com.yuhyeon.devwebide.terminal.service.TerminalLogService;
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

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TerminalLogController 테스트
 *
 * GET /api/workspace-sessions/{workspaceSessionId}/terminal/logs API 정상 응답을 검증합니다.
 */
@WebMvcTest(TerminalLogController.class)
class TerminalLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TerminalLogService terminalLogService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("전체 터미널 로그를 조회한다")
    void getTerminalLogs() throws Exception {
        Long workspaceSessionId = 1L;
        TerminalLogListResponse response = createTerminalLogListResponse(
                workspaceSessionId,
                2L
        );

        given(terminalLogService.getTerminalLogs(workspaceSessionId, null))
                .willReturn(response);

        mockMvc.perform(get(
                                "/api/workspace-sessions/{workspaceSessionId}/terminal/logs",
                                workspaceSessionId
                        )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.workspaceSessionId").value(workspaceSessionId))
                .andExpect(jsonPath("$.lastSequenceNo").value(2L))
                .andExpect(jsonPath("$.logs[0].id").value(1L))
                .andExpect(jsonPath("$.logs[0].sequenceNo").value(1L))
                .andExpect(jsonPath("$.logs[0].streamType").value("STDOUT"))
                .andExpect(jsonPath("$.logs[0].content").value("hello"))
                .andExpect(jsonPath("$.logs[0].createdAt").exists());

        then(terminalLogService).should()
                .getTerminalLogs(workspaceSessionId, null);
    }

    @Test
    @DisplayName("특정 sequenceNo 이후 터미널 로그를 조회한다")
    void getTerminalLogsAfterSequenceNo() throws Exception {
        Long workspaceSessionId = 1L;
        Long afterSequenceNo = 10L;
        TerminalLogListResponse response = createTerminalLogListResponse(
                workspaceSessionId,
                12L
        );

        given(terminalLogService.getTerminalLogs(workspaceSessionId, afterSequenceNo))
                .willReturn(response);

        mockMvc.perform(get(
                                "/api/workspace-sessions/{workspaceSessionId}/terminal/logs",
                                workspaceSessionId
                        )
                        .param("afterSequenceNo", afterSequenceNo.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.workspaceSessionId").value(workspaceSessionId))
                .andExpect(jsonPath("$.lastSequenceNo").value(12L))
                .andExpect(jsonPath("$.logs[0].id").value(11L))
                .andExpect(jsonPath("$.logs[0].sequenceNo").value(11L))
                .andExpect(jsonPath("$.logs[0].streamType").value("STDOUT"))
                .andExpect(jsonPath("$.logs[0].content").value("hello"))
                .andExpect(jsonPath("$.logs[0].createdAt").exists());

        then(terminalLogService).should()
                .getTerminalLogs(workspaceSessionId, afterSequenceNo);
    }

    private TerminalLogListResponse createTerminalLogListResponse(
            Long workspaceSessionId,
            Long lastSequenceNo
    ) {
        Long logId = lastSequenceNo == 2L ? 1L : 11L;
        Long sequenceNo = lastSequenceNo == 2L ? 1L : 11L;

        TerminalLogResponse log = new TerminalLogResponse(
                logId,
                sequenceNo,
                TerminalStreamType.STDOUT,
                "hello",
                LocalDateTime.of(2026, 7, 7, 10, 0)
        );

        return new TerminalLogListResponse(
                workspaceSessionId,
                List.of(log),
                lastSequenceNo
        );
    }
}
