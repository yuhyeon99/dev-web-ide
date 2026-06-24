package com.yuhyeon.devwebide.runtime.controller;

import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.runtime.dto.RuntimeResponse;
import com.yuhyeon.devwebide.runtime.service.RuntimeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 런타임 API 컨트롤러 테스트
 *
 * GET /api/runtimes API 응답을 검증합니다.
 */
@WebMvcTest(RuntimeController.class) // Controller만 테스트하기 위해 사용하는 테스트 어노테이션
class RuntimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RuntimeService runtimeService;

    @Test
    @DisplayName("활성 런타임 목록 조회")
    void getRuntimes() throws Exception {
        List<RuntimeResponse> responses = List.of(
                new RuntimeResponse(
                        1L,
                        "node-20",
                        "Node.js 20",
                        "20",
                        "node:20",
                        RuntimeLanguage.NODE
                ),
                new RuntimeResponse(
                        2L,
                        "python-3.12",
                        "Python 3.12",
                        "3.12",
                        "python:3.12",
                        RuntimeLanguage.PYTHON
                )
        );

        when(runtimeService.getActiveRuntimes())
                .thenReturn(responses);

        mockMvc.perform(get("/api/runtimes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))

                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("node-20"))
                .andExpect(jsonPath("$[0].displayName").value("Node.js 20"))
                .andExpect(jsonPath("$[0].version").value("20"))
                .andExpect(jsonPath("$[0].dockerImage").value("node:20"))
                .andExpect(jsonPath("$[0].language").value("NODE"))

                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("python-3.12"))
                .andExpect(jsonPath("$[1].displayName").value("Python 3.12"))
                .andExpect(jsonPath("$[1].version").value("3.12"))
                .andExpect(jsonPath("$[1].dockerImage").value("python:3.12"))
                .andExpect(jsonPath("$[1].language").value("PYTHON"));
    }
}