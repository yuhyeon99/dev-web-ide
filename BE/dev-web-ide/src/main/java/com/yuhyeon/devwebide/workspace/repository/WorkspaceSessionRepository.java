package com.yuhyeon.devwebide.workspace.repository;

import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import com.yuhyeon.devwebide.workspace.domain.WorkspaceSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 워크스페이스 실행 세션 Repository
 *
 * 프로젝트 실행 세션 조회, 사용자/게스트 기준 실행 중 세션 조회,
 * 특정 프로젝트의 최근 실행 세션 조회에 사용합니다.
 */
public interface WorkspaceSessionRepository extends JpaRepository<WorkspaceSession, Long> {

    /**
     * 프로젝트 기준 워크스페이스 세션 목록 조회
     *
     * @param projectId 프로젝트 ID
     * @return 해당 프로젝트의 워크스페이스 세션 목록
     */
    List<WorkspaceSession> findByProjectIdOrderByStartedAtDesc(Long projectId);

    /**
     * 프로젝트와 상태 기준 워크스페이스 세션 목록 조회
     *
     * @param projectId 프로젝트 ID
     * @param status 워크스페이스 세션 상태
     * @return 해당 프로젝트의 특정 상태 세션 목록
     */
    List<WorkspaceSession> findByProjectIdAndStatusOrderByStartedAtDesc(
            Long projectId,
            WorkspaceSessionStatus status
    );

    /**
     * 회원 사용자 기준 특정 상태의 워크스페이스 세션 목록 조회
     *
     * @param userId 사용자 ID
     * @param status 워크스페이스 세션 상태
     * @return 사용자의 특정 상태 세션 목록
     */
    List<WorkspaceSession> findByUserIdAndStatusOrderByStartedAtDesc(
            Long userId,
            WorkspaceSessionStatus status
    );

    /**
     * 게스트 세션 기준 특정 상태의 워크스페이스 세션 목록 조회
     *
     * @param guestSessionId 게스트 세션 ID
     * @param status 워크스페이스 세션 상태
     * @return 게스트의 특정 상태 세션 목록
     */
    List<WorkspaceSession> findByGuestSessionIdAndStatusOrderByStartedAtDesc(
            Long guestSessionId,
            WorkspaceSessionStatus status
    );

    /**
     * 프로젝트 기준 가장 최근 특정 상태의 워크스페이스 세션 조회
     *
     * 실행 중인 기존 세션 재사용 여부를 판단할 때 사용할 수 있습니다.
     *
     * @param projectId 프로젝트 ID
     * @param status 워크스페이스 세션 상태
     * @return 가장 최근 워크스페이스 세션
     */
    Optional<WorkspaceSession> findTopByProjectIdAndStatusOrderByStartedAtDesc(
            Long projectId,
            WorkspaceSessionStatus status
    );

    /**
     * 회원 사용자의 특정 프로젝트 실행 세션 존재 여부 확인
     *
     * @param projectId 프로젝트 ID
     * @param userId 사용자 ID
     * @param status 워크스페이스 세션 상태
     * @return 존재 여부
     */
    boolean existsByProjectIdAndUserIdAndStatus(
            Long projectId,
            Long userId,
            WorkspaceSessionStatus status
    );

    /**
     * 게스트의 특정 프로젝트 실행 세션 존재 여부 확인
     *
     * @param projectId 프로젝트 ID
     * @param guestSessionId 게스트 세션 ID
     * @param status 워크스페이스 세션 상태
     * @return 존재 여부
     */
    boolean existsByProjectIdAndGuestSessionIdAndStatus(
            Long projectId,
            Long guestSessionId,
            WorkspaceSessionStatus status
    );
}