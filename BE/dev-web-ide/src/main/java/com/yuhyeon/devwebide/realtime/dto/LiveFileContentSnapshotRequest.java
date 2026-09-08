package com.yuhyeon.devwebide.realtime.dto;

/**
 * 새로 접속한 클라이언트가 현재 live file content를 요청할 때 사용합니다.
 */
public record LiveFileContentSnapshotRequest(
        String clientId
) {
}
