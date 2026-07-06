package com.yuhyeon.devwebide.execution.dto;

import com.yuhyeon.devwebide.execution.domain.ContainerInstance;
import com.yuhyeon.devwebide.runtime.domain.RuntimeLanguage;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSessionStatus;

/**
 * 프로젝트 실행 응답 DTO
 *
 * 프로젝트 실행 요청 후 생성된 워크스페이스 세션과
 * 컨테이너 실행 정보를 반환합니다.
 */
public record ProjectRunResponse(
        Long projectId,
        Long workspaceSessionId,
        Long containerInstanceId,
        String runtimeName,
        String runtimeDisplayName,
        RuntimeLanguage runtimeLanguage,
        String dockerImage,
        WorkspaceSessionStatus workspaceStatus,
        String efsMountPath,
        ContainerInstanceResponse container
) {

    /**
     * WorkspaceSession, ContainerInstance 엔티티를 실행 응답 DTO로 변환합니다.
     *
     * @param workspaceSession 워크스페이스 실행 세션
     * @param containerInstance 컨테이너 실행 정보
     * @return 프로젝트 실행 응답 DTO
     */
    public static ProjectRunResponse from(
            WorkspaceSession workspaceSession,
            ContainerInstance containerInstance
    ) {
        return new ProjectRunResponse(
                workspaceSession.getProject().getId(),
                workspaceSession.getId(),
                containerInstance.getId(),
                workspaceSession.getRuntime().getName(),
                workspaceSession.getRuntime().getDisplayName(),
                workspaceSession.getRuntime().getLanguage(),
                containerInstance.getDockerImage(),
                workspaceSession.getStatus(),
                containerInstance.getEfsMountPath(),
                ContainerInstanceResponse.from(containerInstance)
        );
    }
}