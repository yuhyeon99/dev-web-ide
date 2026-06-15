package com.yuhyeon.devwebide.user.domain;

/**
 * 사용자의 계정 상태를 정의하는 열거형입니다.
 */
public enum UserStatus {
    /** 활성 상태 */
    ACTIVE,
    /** 비활성 상태 */
    INACTIVE,
    /** 삭제(탈퇴) 상태 */
    DELETED
}
