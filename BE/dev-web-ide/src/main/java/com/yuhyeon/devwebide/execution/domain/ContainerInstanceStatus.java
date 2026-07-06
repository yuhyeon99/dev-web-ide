package com.yuhyeon.devwebide.execution.domain;

/**
 * 컨테이너 실행 상태
 *
 * STARTING: 컨테이너 시작 요청 후 실행 준비 중
 * RUNNING: 컨테이너 실행 중
 * STOPPED: 컨테이너 정상 종료
 * FAILED: 컨테이너 실행 실패
 */
public enum ContainerInstanceStatus {
    STARTING,
    RUNNING,
    STOPPED,
    FAILED
}