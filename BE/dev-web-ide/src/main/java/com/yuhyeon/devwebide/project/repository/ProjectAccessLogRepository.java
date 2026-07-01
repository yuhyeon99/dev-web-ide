package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.ProjectAccessLog;
import com.yuhyeon.devwebide.project.domain.ProjectAccessType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectAccessLogRepository extends JpaRepository<ProjectAccessLog, Long> {

    /**
     * 회원 사용자의 프로젝트 접근 기록 조회
     *
     * GET /api/projects/recent 에서
     * 회원 사용자의 최근 프로젝트 목록을 조회할 때 사용합니다.
     */
    List<ProjectAccessLog> findByUserIdAndAccessTypeOrderByAccessedAtDesc(
            Long userId,
            ProjectAccessType accessType
    );

    /**
     * 게스트 사용자의 프로젝트 접근 기록 조회
     *
     * 게스트 세션 기준 최근 프로젝트 목록을 조회할 때 사용합니다.
     */
    List<ProjectAccessLog> findByGuestSessionIdAndAccessTypeOrderByAccessedAtDesc(
            Long guestSessionId,
            ProjectAccessType accessType
    );

    /**
     * 특정 프로젝트의 접근 기록 조회
     *
     * 프로젝트별 접근 이력을 확인하거나
     * 디버깅/관리 화면에서 사용할 수 있습니다.
     */
    List<ProjectAccessLog> findByProjectIdOrderByAccessedAtDesc(
            Long projectId
    );

    /**
     * 특정 프로젝트의 접근 유형별 기록 조회
     *
     * OPEN, RUN, SAVE 같은 접근 유형별 이력을 조회할 때 사용합니다.
     */
    List<ProjectAccessLog> findByProjectIdAndAccessTypeOrderByAccessedAtDesc(
            Long projectId,
            ProjectAccessType accessType
    );

    /**
     * 회원 사용자의 특정 프로젝트 접근 기록 조회
     *
     * 특정 회원이 특정 프로젝트를 언제 열었는지 확인할 때 사용합니다.
     */
    List<ProjectAccessLog> findByProjectIdAndUserIdAndAccessTypeOrderByAccessedAtDesc(
            Long projectId,
            Long userId,
            ProjectAccessType accessType
    );

    /**
     * 게스트 사용자의 특정 프로젝트 접근 기록 조회
     *
     * 특정 게스트 세션이 특정 프로젝트를 언제 열었는지 확인할 때 사용합니다.
     */
    List<ProjectAccessLog> findByProjectIdAndGuestSessionIdAndAccessTypeOrderByAccessedAtDesc(
            Long projectId,
            Long guestSessionId,
            ProjectAccessType accessType
    );
}