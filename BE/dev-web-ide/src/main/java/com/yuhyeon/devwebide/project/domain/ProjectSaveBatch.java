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
 * 프로젝트 저장 배치 엔티티
 *
 * 열린 dirty 파일들을 일괄 저장할 때
 * 하나의 저장 요청 단위를 관리합니다.
 *
 * 실제 파일 원본은 EFS에 저장하고,
 * 저장 요청 결과와 저장 파일 수는 DB에 기록합니다.
 */
@Entity
@Table(name = "project_save_batches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ProjectSaveBatch {

    /**
     * 프로젝트 저장 배치 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 저장이 발생한 프로젝트
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * 저장을 수행한 회원 사용자
     *
     * 회원 사용자가 저장한 경우 값이 존재합니다.
     * 게스트 사용자가 저장한 경우 null입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 저장을 수행한 게스트 세션
     *
     * 게스트 사용자가 저장한 경우 값이 존재합니다.
     * 회원 사용자가 저장한 경우 null입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_session_id")
    private GuestSession guestSession;

    /**
     * 저장 배치 상태
     *
     * SUCCESS, FAILED, PARTIAL
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProjectSaveBatchStatus status;

    /**
     * 저장된 파일 수
     */
    @Column(name = "saved_file_count", nullable = false)
    private Integer savedFileCount;

    /**
     * 저장 일시
     */
    @CreatedDate
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    /**
     * ProjectSaveBatch 생성자
     *
     * 회원 저장 배치는 user만 가져야 하고,
     * 게스트 저장 배치는 guestSession만 가져야 합니다.
     *
     * @param project 저장 대상 프로젝트
     * @param user 저장을 수행한 회원 사용자
     * @param guestSession 저장을 수행한 게스트 세션
     * @param status 저장 배치 상태
     * @param savedFileCount 저장된 파일 수
     */
    @Builder
    public ProjectSaveBatch(
            Project project,
            User user,
            GuestSession guestSession,
            ProjectSaveBatchStatus status,
            Integer savedFileCount
    ) {
        validateRequiredFields(project, status, savedFileCount);
        validateSaveOwner(user, guestSession);

        this.project = project;
        this.user = user;
        this.guestSession = guestSession;
        this.status = status;
        this.savedFileCount = savedFileCount;
    }

    /**
     * 필수 값 검증
     *
     * @param project 저장 대상 프로젝트
     * @param status 저장 배치 상태
     * @param savedFileCount 저장된 파일 수
     */
    private void validateRequiredFields(
            Project project,
            ProjectSaveBatchStatus status,
            Integer savedFileCount
    ) {
        if (project == null) {
            throw new IllegalArgumentException("프로젝트는 필수입니다.");
        }

        if (status == null) {
            throw new IllegalArgumentException("저장 배치 상태는 필수입니다.");
        }

        if (savedFileCount == null || savedFileCount < 0) {
            throw new IllegalArgumentException("저장된 파일 수는 0 이상이어야 합니다.");
        }
    }

    /**
     * 저장 주체 검증
     *
     * 회원 사용자 또는 게스트 세션 중 하나만 지정되어야 합니다.
     *
     * @param user 저장을 수행한 회원 사용자
     * @param guestSession 저장을 수행한 게스트 세션
     */
    private void validateSaveOwner(User user, GuestSession guestSession) {
        boolean hasUser = user != null;
        boolean hasGuestSession = guestSession != null;

        if (hasUser == hasGuestSession) {
            throw new IllegalArgumentException(
                    "회원 사용자 또는 게스트 세션 중 하나만 지정해야 합니다."
            );
        }
    }

    /**
     * 회원 저장 배치 여부 확인
     *
     * @return 회원 저장 배치 여부
     */
    public boolean isUserSaveBatch() {
        return this.user != null;
    }

    /**
     * 게스트 저장 배치 여부 확인
     *
     * @return 게스트 저장 배치 여부
     */
    public boolean isGuestSaveBatch() {
        return this.guestSession != null;
    }

    /**
     * 저장 성공 여부 확인
     *
     * @return 저장 성공 여부
     */
    public boolean isSuccess() {
        return this.status == ProjectSaveBatchStatus.SUCCESS;
    }

    /**
     * 저장 실패 여부 확인
     *
     * @return 저장 실패 여부
     */
    public boolean isFailed() {
        return this.status == ProjectSaveBatchStatus.FAILED;
    }

    /**
     * 일부 저장 성공 여부 확인
     *
     * @return 일부 저장 성공 여부
     */
    public boolean isPartial() {
        return this.status == ProjectSaveBatchStatus.PARTIAL;
    }

    /**
     * 저장 배치 실패 처리
     */
    public void markFailed() {
        this.status = ProjectSaveBatchStatus.FAILED;
    }

    /**
     * 저장 배치 일부 성공 처리
     *
     * @param savedFileCount 저장된 파일 수
     */
    public void markPartial(Integer savedFileCount) {
        if (savedFileCount == null || savedFileCount < 0) {
            throw new IllegalArgumentException("저장된 파일 수는 0 이상이어야 합니다.");
        }

        this.status = ProjectSaveBatchStatus.PARTIAL;
        this.savedFileCount = savedFileCount;
    }

    /**
     * 저장 배치 성공 처리
     *
     * @param savedFileCount 저장된 파일 수
     */
    public void markSuccess(Integer savedFileCount) {
        if (savedFileCount == null || savedFileCount < 0) {
            throw new IllegalArgumentException("저장된 파일 수는 0 이상이어야 합니다.");
        }

        this.status = ProjectSaveBatchStatus.SUCCESS;
        this.savedFileCount = savedFileCount;
    }
}