package com.yuhyeon.devwebide.runtime.controller;

import com.yuhyeon.devwebide.runtime.dto.RuntimeResponse;
import com.yuhyeon.devwebide.runtime.service.RuntimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 런타임 API 컨트롤러
 *
 * 프로젝트 생성 시 선택 가능한 런타임 목록을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/runtimes")
public class RuntimeController {

    private final RuntimeService runtimeService;

    /**
     * 활성 런타임 목록 조회
     *
     * ACTIVE 상태인 런타임만 조회합니다.
     *
     * @return 활성 런타임 목록
     */
    @GetMapping
    public ResponseEntity<List<RuntimeResponse>> getRuntimes() {
        List<RuntimeResponse> runtimes = runtimeService.getActiveRuntimes();

        return ResponseEntity.ok(runtimes);
    }
}