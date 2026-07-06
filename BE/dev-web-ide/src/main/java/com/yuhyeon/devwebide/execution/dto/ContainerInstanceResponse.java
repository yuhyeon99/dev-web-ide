package com.yuhyeon.devwebide.execution.dto;

import com.yuhyeon.devwebide.execution.domain.ContainerInstance;
import com.yuhyeon.devwebide.execution.domain.ContainerInstanceStatus;

/**
 * 컨테이너 실행 정보 응답 DTO
 *
 * 프로젝트 실행 시 생성된 실제 컨테이너 실행 정보를 반환합니다.
 */
public record ContainerInstanceResponse(
        Long id,
        String provider,
        String taskArn,
        String containerId,
        String dockerImage,
        ContainerInstanceStatus status,
        String efsMountPath
) {

    /**
     * ContainerInstance 엔티티를 응답 DTO로 변환합니다.
     *
     * @param containerInstance 컨테이너 실행 정보
     * @return 컨테이너 실행 응답 DTO
     */
    public static ContainerInstanceResponse from(
            ContainerInstance containerInstance
    ) {
        return new ContainerInstanceResponse(
                containerInstance.getId(),
                containerInstance.getProvider(),
                containerInstance.getTaskArn(),
                containerInstance.getContainerId(),
                containerInstance.getDockerImage(),
                containerInstance.getStatus(),
                containerInstance.getEfsMountPath()
        );
    }
}