package com.yuhyeon.devwebide.workspace.domain;

/**
 * 워크스페이스 실행 세션 상태
 *
 * STARTING: 실행 요청 후 컨테이너 준비 중
 * RUNNING: 실행 중
 * STOPPED: 정상 종료
 * FAILED: 실행 실패
 */
public enum WorkspaceSessionStatus {
    STARTING,
    RUNNING,
    STOPPED,
    FAILED
}