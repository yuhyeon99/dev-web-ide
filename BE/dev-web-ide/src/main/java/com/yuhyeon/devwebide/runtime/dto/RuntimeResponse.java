package com.yuhyeon.devwebide.runtime.dto;

import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;

/**
 * 런타임 목록 조회 응답 DTO
 *
 * 프로젝트 생성 화면에서 선택 가능한 런타임 정보를 반환합니다.
 */
public record RuntimeResponse(
        Long id,
        String name,
        String displayName,
        String version,
        String dockerImage,
        RuntimeLanguage language
) {

    /**
     * Runtime 엔티티를 응답 DTO로 변환합니다.
     *
     * @param runtime 런타임 엔티티
     * @return 런타임 응답 DTO
     */
    public static RuntimeResponse from(Runtime runtime) {
        return new RuntimeResponse(
                runtime.getId(),
                runtime.getName(),
                runtime.getDisplayName(),
                runtime.getVersion(),
                runtime.getDockerImage(),
                runtime.getLanguage()
        );
    }
}