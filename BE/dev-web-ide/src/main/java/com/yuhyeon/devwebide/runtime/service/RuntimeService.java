package com.yuhyeon.devwebide.runtime.service;

import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.runtime.domain.RuntimeStatus;
import com.yuhyeon.devwebide.runtime.dto.RuntimeResponse;
import com.yuhyeon.devwebide.runtime.repository.RuntimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 런타임 서비스
 *
 * 프로젝트 생성 시 선택 가능한 런타임 목록을 조회합니다.
 */
@Service
@RequiredArgsConstructor // 필수 필드만 받는 생성자를 자동으로 만들어줌
@Transactional(readOnly = true)
public class RuntimeService {

    private final RuntimeRepository runtimeRepository;

    /**
     * 활성 상태의 런타임 목록을 조회합니다.
     *
     * ACTIVE 상태인 런타임만 프로젝트 생성 화면에 노출합니다.
     *
     * @return 활성 런타임 목록
     */
    public List<RuntimeResponse> getActiveRuntimes() {
        return runtimeRepository.findByStatus(RuntimeStatus.ACTIVE)
                .stream() // 조회된 List<Runtime>을 반복 처리하기 좋은 Stream으로 바꾼다.
                .sorted(Comparator.comparing(Runtime::getId)) // Runtime의 id 값을 기준으로 오름차순 정렬
                .map(RuntimeResponse::from) // 각 Runtime 엔티티를 RuntimeResponse DTO로 변환
                .toList(); // 다시 List로 만든다.
    }
}