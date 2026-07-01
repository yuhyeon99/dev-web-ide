package com.yuhyeon.devwebide.project.domain;

import com.yuhyeon.devwebide.user.domain.GuestSession;
import com.yuhyeon.devwebide.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 프로젝트 접근 기록 엔티티
 *
 * 사용자가 프로젝트를 열거나 실행하거나 저장한 기록을 관리합니다.
 * 최근 프로젝트 조회, 팀 프로젝트 최근 접근 조회,
 * 프로젝트 열기 이력 관리에 사용됩니다.
 */
@Entity
@Table(name = "project_access_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ProjectAccessLog {

    /**
     * 프로젝트 접근 기록 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 접근한 프로젝트
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * 접근한 회원 사용자
     *
     * 회원 사용자가 프로젝트를 연 경우 값이 존재합니다.
     * 게스트 사용자인 경우 null입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 접근한 게스트 세션
     *
     * 게스트 사용자가 프로젝트를 연 경우 값이 존재합니다.
     * 회원 사용자인 경우 null입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_session_id")
    private GuestSession guestSession;

    /**
     * 프로젝트 접근 유형
     *
     * OPEN, RUN, SAVE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 30)
    private ProjectAccessType accessType;

    /**
     * 접근 일시
     */
    @CreatedDate
    @Column(name = "accessed_at", nullable = false, updatable = false)
    private LocalDateTime accessedAt;

    /**
     * ProjectAccessLog 생성자
     *
     * 회원 접근 기록은 user만 가져야 하고,
     * 게스트 접근 기록은 guestSession만 가져야 합니다.
     *
     * @param project 접근한 프로젝트
     * @param user 접근한 회원 사용자
     * @param guestSession 접근한 게스트 세션
     * @param accessType 접근 유형
     */
    @Builder
    public ProjectAccessLog(
            Project project,
            User user,
            GuestSession guestSession,
            ProjectAccessType accessType
    ) {
        validateRequiredFields(project, accessType);
        validateAccessOwner(user, guestSession);

        this.project = project;
        this.user = user;
        this.guestSession = guestSession;
        this.accessType = accessType;
    }

    /**
     * 필수 값 검증
     *
     * @param project 접근한 프로젝트
     * @param accessType 접근 유형
     */
    private void validateRequiredFields(
            Project project,
            ProjectAccessType accessType
    ) {
        if (project == null) {
            throw new IllegalArgumentException("프로젝트는 필수입니다.");
        }

        if (accessType == null) {
            throw new IllegalArgumentException("프로젝트 접근 유형은 필수입니다.");
        }
    }

    /**
     * 접근 주체 검증
     *
     * 회원 사용자 또는 게스트 세션 중 하나만 지정되어야 합니다.
     *
     * @param user 회원 사용자
     * @param guestSession 게스트 세션
     */
    private void validateAccessOwner(
            User user,
            GuestSession guestSession
    ) {
        boolean hasUser = user != null;
        boolean hasGuestSession = guestSession != null;

        if (hasUser == hasGuestSession) {
            throw new IllegalArgumentException(
                    "회원 사용자 또는 게스트 세션 중 하나만 지정해야 합니다."
            );
        }
    }

    /**
     * 회원 접근 기록 여부 확인
     *
     * @return 회원 접근 기록 여부
     */
    public boolean isUserAccess() {
        return this.user != null;
    }

    /**
     * 게스트 접근 기록 여부 확인
     *
     * @return 게스트 접근 기록 여부
     */
    public boolean isGuestAccess() {
        return this.guestSession != null;
    }

    /**
     * 프로젝트 열기 기록 여부 확인
     *
     * @return 프로젝트 열기 기록 여부
     */
    public boolean isOpenAccess() {
        return this.accessType == ProjectAccessType.OPEN;
    }

    /**
     * 프로젝트 실행 기록 여부 확인
     *
     * @return 프로젝트 실행 기록 여부
     */
    public boolean isRunAccess() {
        return this.accessType == ProjectAccessType.RUN;
    }

    /**
     * 프로젝트 저장 기록 여부 확인
     *
     * @return 프로젝트 저장 기록 여부
     */
    public boolean isSaveAccess() {
        return this.accessType == ProjectAccessType.SAVE;
    }
}