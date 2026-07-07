package com.yuhyeon.devwebide.terminal.domain;

/**
 * 터미널 로그 스트림 타입
 *
 * STDOUT: 표준 출력
 * STDERR: 표준 에러
 * SYSTEM: 시스템 메시지
 */
public enum TerminalStreamType {
    STDOUT,
    STDERR,
    SYSTEM
}
