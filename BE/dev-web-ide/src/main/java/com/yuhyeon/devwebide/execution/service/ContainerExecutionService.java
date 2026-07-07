package com.yuhyeon.devwebide.execution.service;

import com.yuhyeon.devwebide.execution.domain.ContainerInstance;
import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;

/**
 * 컨테이너 실행 인터페이스
 *
 * ProjectRunService는 실제 ECS/Fargate 구현에 직접 의존하지 않고,
 * 이 인터페이스를 통해 컨테이너 실행을 요청합니다.
 */
public interface ContainerExecutionService {

    /**
     * 프로젝트 실행 컨테이너를 시작합니다.
     *
     * @param project 실행할 프로젝트
     * @param runtime 프로젝트 런타임
     * @param workspaceSession 워크스페이스 실행 세션
     * @return 컨테이너 실행 결과
     */
    ContainerStartResult start(
            Project project,
            Runtime runtime,
            WorkspaceSession workspaceSession
    );

    void stop(ContainerInstance containerInstance);
}
