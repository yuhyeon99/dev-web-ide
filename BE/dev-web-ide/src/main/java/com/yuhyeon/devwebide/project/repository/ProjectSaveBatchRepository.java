package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.ProjectSaveBatch;
import com.yuhyeon.devwebide.project.domain.ProjectSaveBatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 프로젝트 저장 배치 Repository
 *
 * 프로젝트 파일 저장 요청 단위인 ProjectSaveBatch를 조회합니다.
 * 파일 저장 API에서 저장 이력, 저장 상태, 사용자별 저장 기록 조회에 사용합니다.
 */
public interface ProjectSaveBatchRepository extends JpaRepository<ProjectSaveBatch, Long> {

    /**
     * 프로젝트 기준 저장 배치 목록 조회
     *
     * @param projectId 프로젝트 ID
     * @return 최근 저장 순 저장 배치 목록
     */
    List<ProjectSaveBatch> findByProjectIdOrderBySavedAtDesc(Long projectId);

    /**
     * 프로젝트와 저장 상태 기준 저장 배치 목록 조회
     *
     * @param projectId 프로젝트 ID
     * @param status 저장 배치 상태
     * @return 최근 저장 순 저장 배치 목록
     */
    List<ProjectSaveBatch> findByProjectIdAndStatusOrderBySavedAtDesc(
            Long projectId,
            ProjectSaveBatchStatus status
    );

    /**
     * 회원 사용자 기준 저장 배치 목록 조회
     *
     * @param userId 회원 사용자 ID
     * @return 최근 저장 순 저장 배치 목록
     */
    List<ProjectSaveBatch> findByUserIdOrderBySavedAtDesc(Long userId);

    /**
     * 회원 사용자와 저장 상태 기준 저장 배치 목록 조회
     *
     * @param userId 회원 사용자 ID
     * @param status 저장 배치 상태
     * @return 최근 저장 순 저장 배치 목록
     */
    List<ProjectSaveBatch> findByUserIdAndStatusOrderBySavedAtDesc(
            Long userId,
            ProjectSaveBatchStatus status
    );

    /**
     * 게스트 세션 기준 저장 배치 목록 조회
     *
     * @param guestSessionId 게스트 세션 ID
     * @return 최근 저장 순 저장 배치 목록
     */
    List<ProjectSaveBatch> findByGuestSessionIdOrderBySavedAtDesc(Long guestSessionId);

    /**
     * 게스트 세션과 저장 상태 기준 저장 배치 목록 조회
     *
     * @param guestSessionId 게스트 세션 ID
     * @param status 저장 배치 상태
     * @return 최근 저장 순 저장 배치 목록
     */
    List<ProjectSaveBatch> findByGuestSessionIdAndStatusOrderBySavedAtDesc(
            Long guestSessionId,
            ProjectSaveBatchStatus status
    );

    /**
     * 프로젝트의 가장 최근 저장 배치 조회
     *
     * @param projectId 프로젝트 ID
     * @return 가장 최근 저장 배치
     */
    ProjectSaveBatch findTopByProjectIdOrderBySavedAtDesc(Long projectId);
}