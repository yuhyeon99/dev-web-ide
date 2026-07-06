package com.yuhyeon.devwebide.execution.dto;

/**
 * 프로젝트 실행 요청 DTO
 *
 * 초기 인증 연동 전까지는 userId 또는 guestSessionId를 요청으로 받습니다.
 * 회원 실행 요청이면 userId만 존재하고,
 * 게스트 실행 요청이면 guestSessionId만 존재해야 합니다.
 */
public record ProjectRunRequest(
        Long userId,
        Long guestSessionId
) {

    /**
     * 회원 실행 요청 여부
     *
     * @return 회원 실행 요청이면 true
     */
    public boolean isUserRequest() {
        return userId != null;
    }

    /**
     * 게스트 실행 요청 여부
     *
     * @return 게스트 실행 요청이면 true
     */
    public boolean isGuestRequest() {
        return guestSessionId != null;
    }
}