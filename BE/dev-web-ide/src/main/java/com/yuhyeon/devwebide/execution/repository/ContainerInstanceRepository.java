package com.yuhyeon.devwebide.execution.repository;

import com.yuhyeon.devwebide.execution.domain.ContainerInstance;
import com.yuhyeon.devwebide.execution.domain.ContainerInstanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContainerInstanceRepository
        extends JpaRepository<ContainerInstance, Long> {

    /**
     * 워크스페이스 세션 ID 기준 컨테이너 인스턴스 목록 조회
     *
     * 하나의 WorkspaceSession에서 컨테이너가 재시작될 수 있으므로
     * 목록으로 조회합니다.
     */
    List<ContainerInstance> findByWorkspaceSessionIdOrderByCreatedAtDesc(
            Long workspaceSessionId
    );

    /**
     * 워크스페이스 세션 ID 기준 최신 컨테이너 인스턴스 조회
     *
     * 특정 워크스페이스 세션의 가장 최근 컨테이너 실행 정보를 조회할 때 사용합니다.
     */
    Optional<ContainerInstance> findTopByWorkspaceSessionIdOrderByCreatedAtDesc(
            Long workspaceSessionId
    );

    /**
     * 컨테이너 상태 기준 목록 조회
     *
     * RUNNING, STOPPED, FAILED 상태별 컨테이너 목록을 조회할 때 사용합니다.
     */
    List<ContainerInstance> findByStatusOrderByCreatedAtDesc(
            ContainerInstanceStatus status
    );

    /**
     * 워크스페이스 세션 ID와 상태 기준 최신 컨테이너 인스턴스 조회
     *
     * 특정 세션의 실행 중 컨테이너를 조회할 때 사용합니다.
     */
    Optional<ContainerInstance> findTopByWorkspaceSessionIdAndStatusOrderByCreatedAtDesc(
            Long workspaceSessionId,
            ContainerInstanceStatus status
    );

    /**
     * 워크스페이스 세션 ID와 상태 기준 컨테이너 존재 여부 확인
     *
     * 이미 실행 중인 컨테이너가 있는지 확인할 때 사용합니다.
     */
    boolean existsByWorkspaceSessionIdAndStatus(
            Long workspaceSessionId,
            ContainerInstanceStatus status
    );

    /**
     * ECS Task ARN 기준 컨테이너 인스턴스 조회
     *
     * ECS/Fargate 연동 후 AWS Task 상태를 동기화할 때 사용합니다.
     */
    Optional<ContainerInstance> findByTaskArn(String taskArn);

    /**
     * 컨테이너 ID 기준 컨테이너 인스턴스 조회
     *
     * 로컬 Docker 또는 ECS 컨테이너 ID로 상태를 조회할 때 사용합니다.
     */
    Optional<ContainerInstance> findByContainerId(String containerId);
}