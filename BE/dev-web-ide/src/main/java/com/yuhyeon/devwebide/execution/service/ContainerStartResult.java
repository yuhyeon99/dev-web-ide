package com.yuhyeon.devwebide.execution.service;

import com.yuhyeon.devwebide.execution.domain.ContainerInstanceStatus;

/**
 * 컨테이너 실행 결과
 *
 * 실제 ECS/Fargate 또는 Local Docker 실행 결과를
 * ProjectRunService에 전달하기 위한 값 객체입니다.
 */
public record ContainerStartResult(
        String provider,
        String taskArn,
        String containerId,
        String dockerImage,
        ContainerInstanceStatus status,
        String efsMountPath
) {
}