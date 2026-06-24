package com.yuhyeon.devwebide.runtime.domain;

/**
 * 런타임 상태
 *
 * 런타임을 프로젝트 생성 화면에 노출할지 여부를 관리합니다.
 */
public enum RuntimeStatus {
    /**
     * 사용 가능한 런타임
     */
    ACTIVE,

    /**
     * 비활성화된 런타임
     */
    INACTIVE
}