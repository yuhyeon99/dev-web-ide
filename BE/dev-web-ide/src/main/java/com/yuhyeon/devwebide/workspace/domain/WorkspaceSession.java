package com.yuhyeon.devwebide.workspace.domain;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.runtime.domain.Runtime;
import com.yuhyeon.devwebide.user.domain.GuestSession;
import com.yuhyeon.devwebide.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 워크스페이스 실행 세션 엔티티
 *
 * 사용자가 프로젝트를 실행한 논리적 세션을 관리합니다.
 * 실제 ECS/Fargate 컨테이너 실행 정보는 ContainerInstance에서 관리하고,
 * WorkspaceSession은 프로젝트 실행 단위의 기준 역할을 합니다.
 */
@Entity
@Table(name = "workspace_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WorkspaceSession {

    /**
     * 워크스페이스 세션 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 실행 대상 프로젝트
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * 실행 런타임
     *
     * 프로젝트 생성 시 선택한 런타임을 기준으로 실행합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "runtime_id", nullable = false)
    private Runtime runtime;

    /**
     * 실행한 회원 사용자
     *
     * 회원이 실행한 경우 값이 존재합니다.
     * 게스트가 실행한 경우 null입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 실행한 게스트 세션
     *
     * 게스트가 실행한 경우 값이 존재합니다.
     * 회원이 실행한 경우 null입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_session_id")
    private GuestSession guestSession;

    /**
     * 워크스페이스 실행 상태
     *
     * STARTING, RUNNING, STOPPED, FAILED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private WorkspaceSessionStatus status;

    /**
     * 실행 시작 일시
     */
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    /**
     * 실행 종료 일시
     *
     * 세션이 종료되기 전까지는 null입니다.
     */
    @Column(name = "stopped_at")
    private LocalDateTime stoppedAt;

    /**
     * 마지막 heartbeat 일시
     *
     * 컨테이너 상태 확인 또는 접속 유지 확인 시 갱신합니다.
     */
    @Column(name = "last_heartbeat_at")
    private LocalDateTime lastHeartbeatAt;

    /**
     * WorkspaceSession 생성자
     *
     * 회원 세션은 user만 가져야 하고,
     * 게스트 세션은 guestSession만 가져야 합니다.
     *
     * startedAt은 워크스페이스 실행 세션이 생성되는 시점의 시간으로 설정합니다.
     * lastHeartbeatAt은 별도 값이 전달되면 해당 값을 사용하고,
     * 전달되지 않으면 startedAt과 동일한 시간으로 초기화합니다.
     *
     * @param project 실행 대상 프로젝트
     * @param runtime 실행 런타임
     * @param user 회원 사용자
     * @param guestSession 게스트 세션
     * @param status 실행 상태
     * @param lastHeartbeatAt 마지막 heartbeat 일시
     */
    @Builder
    public WorkspaceSession(
            Project project,
            Runtime runtime,
            User user,
            GuestSession guestSession,
            WorkspaceSessionStatus status,
            LocalDateTime lastHeartbeatAt
    ) {
        validateRequiredFields(project, runtime, status);
        validateSessionOwner(user, guestSession);

        LocalDateTime now = LocalDateTime.now();

        this.project = project;
        this.runtime = runtime;
        this.user = user;
        this.guestSession = guestSession;
        this.status = status;
        this.startedAt = now;
        this.lastHeartbeatAt = lastHeartbeatAt != null ? lastHeartbeatAt : now;
    }

    /**
     * 필수 값 검증
     *
     * @param project 실행 대상 프로젝트
     * @param runtime 실행 런타임
     * @param status 실행 상태
     */
    private void validateRequiredFields(
            Project project,
            Runtime runtime,
            WorkspaceSessionStatus status
    ) {
        if (project == null) {
            throw new IllegalArgumentException("프로젝트는 필수입니다.");
        }

        if (runtime == null) {
            throw new IllegalArgumentException("런타임은 필수입니다.");
        }

        if (status == null) {
            throw new IllegalArgumentException("워크스페이스 세션 상태는 필수입니다.");
        }
    }

    /**
     * 회원 또는 게스트 실행 주체 검증
     *
     * user와 guestSession 중 하나만 지정되어야 합니다.
     *
     * @param user 회원 사용자
     * @param guestSession 게스트 세션
     */
    private void validateSessionOwner(User user, GuestSession guestSession) {
        boolean hasUser = user != null;
        boolean hasGuestSession = guestSession != null;

        if (hasUser == hasGuestSession) {
            throw new IllegalArgumentException(
                    "회원 사용자 또는 게스트 세션 중 하나만 지정해야 합니다."
            );
        }
    }

    /**
     * 회원 실행 세션 여부 확인
     *
     * @return 회원 실행 세션 여부
     */
    public boolean isUserSession() {
        return this.user != null;
    }

    /**
     * 게스트 실행 세션 여부 확인
     *
     * @return 게스트 실행 세션 여부
     */
    public boolean isGuestSession() {
        return this.guestSession != null;
    }

    /**
     * 실행 중 여부 확인
     *
     * @return 실행 중 여부
     */
    public boolean isRunning() {
        return this.status == WorkspaceSessionStatus.RUNNING;
    }

    /**
     * 종료 여부 확인
     *
     * @return 종료 여부
     */
    public boolean isStopped() {
        return this.status == WorkspaceSessionStatus.STOPPED;
    }

    /**
     * 실패 여부 확인
     *
     * @return 실패 여부
     */
    public boolean isFailed() {
        return this.status == WorkspaceSessionStatus.FAILED;
    }

    /**
     * 실행 중 상태로 변경
     *
     * 컨테이너 실행이 완료되어 실제 실행 상태가 되었을 때 사용합니다.
     *
     * @param lastHeartbeatAt 마지막 heartbeat 일시
     */
    public void markRunning(LocalDateTime lastHeartbeatAt) {
        this.status = WorkspaceSessionStatus.RUNNING;
        this.lastHeartbeatAt = lastHeartbeatAt;
    }

    /**
     * heartbeat 갱신
     *
     * @param lastHeartbeatAt 마지막 heartbeat 일시
     */
    public void refreshHeartbeat(LocalDateTime lastHeartbeatAt) {
        this.lastHeartbeatAt = lastHeartbeatAt;
    }

    /**
     * 세션 정상 종료 처리
     *
     * @param stoppedAt 종료 일시
     */
    public void stop(LocalDateTime stoppedAt) {
        this.status = WorkspaceSessionStatus.STOPPED;
        this.stoppedAt = stoppedAt;
    }

    /**
     * 세션 실패 처리
     *
     * @param stoppedAt 실패 처리 일시
     */
    public void fail(LocalDateTime stoppedAt) {
        this.status = WorkspaceSessionStatus.FAILED;
        this.stoppedAt = stoppedAt;
    }
}