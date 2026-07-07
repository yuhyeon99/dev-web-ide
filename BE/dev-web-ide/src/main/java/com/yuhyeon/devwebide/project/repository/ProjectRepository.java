package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.domain.ProjectType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 프로젝트 ID와 상태로 프로젝트 조회
     *
     * 삭제 처리된 프로젝트를 제외하고
     * ACTIVE 상태의 프로젝트만 상세 조회할 때 사용합니다.
     */
    Optional<Project> findByIdAndStatus(
            Long id,
            ProjectStatus status
    );

    /**
     * 회원 소유 프로젝트 목록 조회
     *
     * GET /api/projects/my 에서
     * 내가 생성한 프로젝트 목록을 조회할 때 사용합니다.
     */
    List<Project> findByOwnerUserIdAndStatusOrderByUpdatedAtDescIdDesc(
            Long ownerUserId,
            ProjectStatus status
    );

    /**
     * 회원 소유 프로젝트를 타입별로 조회
     *
     * 개인 프로젝트와 팀 프로젝트를 구분해서 조회할 때 사용합니다.
     */
    List<Project> findByOwnerUserIdAndProjectTypeAndStatusOrderByUpdatedAtDesc(
            Long ownerUserId,
            ProjectType projectType,
            ProjectStatus status
    );

    /**
     * 게스트 세션 기준 프로젝트 목록 조회
     *
     * 게스트 사용자의 임시 프로젝트 목록을 조회할 때 사용합니다.
     */
    List<Project> findByGuestSessionIdAndStatusOrderByUpdatedAtDesc(
            Long guestSessionId,
            ProjectStatus status
    );

    /**
     * 회원 소유 프로젝트 이름 검색
     *
     * GET /api/projects?filter=created&q=검색어 형태의
     * 프로젝트 검색 기능에서 사용할 수 있습니다.
     */
    List<Project> findByOwnerUserIdAndStatusAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(
            Long ownerUserId,
            ProjectStatus status,
            String name
    );

    /**
     * 게스트 프로젝트 이름 검색
     *
     * 게스트 사용자의 프로젝트 검색 기능에서 사용할 수 있습니다.
     */
    List<Project> findByGuestSessionIdAndStatusAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(
            Long guestSessionId,
            ProjectStatus status,
            String name
    );
}
