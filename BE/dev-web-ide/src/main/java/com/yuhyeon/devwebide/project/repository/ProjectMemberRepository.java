package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.ProjectMember;
import com.yuhyeon.devwebide.project.domain.ProjectMemberRole;
import com.yuhyeon.devwebide.project.domain.ProjectMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    /**
     * 프로젝트 ID와 사용자 ID로 멤버 조회
     *
     * 특정 사용자가 특정 프로젝트의 멤버인지 확인할 때 사용합니다.
     */
    Optional<ProjectMember> findByProjectIdAndUserId(
            Long projectId,
            Long userId
    );

    Optional<ProjectMember> findByIdAndProjectId(
            Long id,
            Long projectId
    );

    /**
     * 프로젝트 ID, 사용자 ID, 상태로 멤버 조회
     *
     * ACTIVE 상태의 멤버만 조회하거나,
     * 초대 상태의 멤버만 조회할 때 사용합니다.
     */
    Optional<ProjectMember> findByProjectIdAndUserIdAndStatus(
            Long projectId,
            Long userId,
            ProjectMemberStatus status
    );

    /**
     * 프로젝트 ID와 사용자 ID로 멤버 존재 여부 확인
     *
     * 같은 프로젝트에 동일 사용자를 중복 초대하지 않기 위해 사용합니다.
     */
    boolean existsByProjectIdAndUserId(
            Long projectId,
            Long userId
    );

    /**
     * 프로젝트의 특정 상태 멤버 목록 조회
     *
     * 프로젝트 상세 화면에서 ACTIVE 멤버 목록을 조회할 때 사용합니다.
     */
    List<ProjectMember> findByProjectIdAndStatusOrderByJoinedAtDesc(
            Long projectId,
            ProjectMemberStatus status
    );

    /**
     * 사용자가 참여 중인 프로젝트 멤버 정보 목록 조회
     *
     * GET /api/projects/shared 에서
     * 나에게 공유된 프로젝트 목록을 조회할 때 사용합니다.
     */
    List<ProjectMember> findByUserIdAndStatusOrderByJoinedAtDesc(
            Long userId,
            ProjectMemberStatus status
    );

    List<ProjectMember> findByUserIdAndStatusInOrderByInvitedAtDescJoinedAtDescIdDesc(
            Long userId,
            List<ProjectMemberStatus> statuses
    );

    /**
     * 프로젝트의 특정 권한과 상태를 가진 멤버 목록 조회
     *
     * 프로젝트 OWNER, EDITOR, VIEWER 등을 구분해서 조회할 때 사용합니다.
     */
    List<ProjectMember> findByProjectIdAndRoleAndStatus(
            Long projectId,
            ProjectMemberRole role,
            ProjectMemberStatus status
    );

    /**
     * 프로젝트에 ACTIVE 상태의 OWNER가 존재하는지 확인
     *
     * 프로젝트 생성자 등록 여부나 OWNER 권한 존재 여부를 검증할 때 사용합니다.
     */
    boolean existsByProjectIdAndRoleAndStatus(
            Long projectId,
            ProjectMemberRole role,
            ProjectMemberStatus status
    );

    boolean existsByProjectIdAndUserIdAndStatusIn(
            Long projectId,
            Long userId,
            List<ProjectMemberStatus> statuses
    );
}
