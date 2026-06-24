package com.yuhyeon.devwebide.project.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 프로젝트 설정 엔티티
 *
 * 프로젝트별 IDE 설정을 관리합니다.
 * Auto Save, Format on Save, 게스트 편집 허용 여부,
 * 커서 위치 공유 여부를 저장합니다.
 */
@Entity
@Table(name = "project_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ProjectSettings {

    /**
     * 프로젝트 설정 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 설정이 적용되는 프로젝트
     *
     * 프로젝트 하나당 설정은 하나만 존재하므로 1:1 관계로 설정합니다.
     *
     * project_id는 단일 컬럼 유니크 제약이므로
     * @Table의 uniqueConstraints 대신
     * @JoinColumn(unique = true)로 설정합니다.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    /**
     * 자동 저장 활성화 여부
     */
    @Column(name = "auto_save_enabled", nullable = false)
    private boolean autoSaveEnabled;

    /**
     * 저장 시 자동 포맷 활성화 여부
     */
    @Column(name = "format_on_save_enabled", nullable = false)
    private boolean formatOnSaveEnabled;

    /**
     * 게스트 편집 허용 여부
     */
    @Column(name = "guest_can_edit", nullable = false)
    private boolean guestCanEdit;

    /**
     * 커서 위치 공유 여부
     */
    @Column(name = "share_cursor_position", nullable = false)
    private boolean shareCursorPosition;

    /**
     * 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * ProjectSettings 생성자
     *
     * @param project 설정 대상 프로젝트
     * @param autoSaveEnabled 자동 저장 활성화 여부
     * @param formatOnSaveEnabled 저장 시 자동 포맷 활성화 여부
     * @param guestCanEdit 게스트 편집 허용 여부
     * @param shareCursorPosition 커서 위치 공유 여부
     */
    @Builder
    public ProjectSettings(
            Project project,
            boolean autoSaveEnabled,
            boolean formatOnSaveEnabled,
            boolean guestCanEdit,
            boolean shareCursorPosition
    ) {
        validateProject(project);

        this.project = project;
        this.autoSaveEnabled = autoSaveEnabled;
        this.formatOnSaveEnabled = formatOnSaveEnabled;
        this.guestCanEdit = guestCanEdit;
        this.shareCursorPosition = shareCursorPosition;
    }

    /**
     * 프로젝트 필수 여부 검증
     *
     * @param project 설정 대상 프로젝트
     */
    private void validateProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("프로젝트는 필수입니다.");
        }
    }

    /**
     * 프로젝트 기본 설정 생성
     *
     * 프로젝트 생성 시 기본 설정을 함께 생성할 때 사용합니다.
     *
     * @param project 설정 대상 프로젝트
     * @return 기본 프로젝트 설정
     */
    public static ProjectSettings createDefault(Project project) {
        return ProjectSettings.builder()
                .project(project)
                .autoSaveEnabled(false)
                .formatOnSaveEnabled(false)
                .guestCanEdit(false)
                .shareCursorPosition(true)
                .build();
    }

    /**
     * 자동 저장 설정 변경
     *
     * @param autoSaveEnabled 자동 저장 활성화 여부
     */
    public void changeAutoSaveEnabled(boolean autoSaveEnabled) {
        this.autoSaveEnabled = autoSaveEnabled;
    }

    /**
     * 저장 시 자동 포맷 설정 변경
     *
     * @param formatOnSaveEnabled 저장 시 자동 포맷 활성화 여부
     */
    public void changeFormatOnSaveEnabled(boolean formatOnSaveEnabled) {
        this.formatOnSaveEnabled = formatOnSaveEnabled;
    }

    /**
     * 게스트 편집 허용 설정 변경
     *
     * @param guestCanEdit 게스트 편집 허용 여부
     */
    public void changeGuestCanEdit(boolean guestCanEdit) {
        this.guestCanEdit = guestCanEdit;
    }

    /**
     * 커서 위치 공유 설정 변경
     *
     * @param shareCursorPosition 커서 위치 공유 여부
     */
    public void changeShareCursorPosition(boolean shareCursorPosition) {
        this.shareCursorPosition = shareCursorPosition;
    }

    /**
     * 프로젝트 설정 일괄 변경
     *
     * PATCH /api/projects/{projectId}/settings 에서 사용합니다.
     *
     * @param autoSaveEnabled 자동 저장 활성화 여부
     * @param formatOnSaveEnabled 저장 시 자동 포맷 활성화 여부
     * @param guestCanEdit 게스트 편집 허용 여부
     * @param shareCursorPosition 커서 위치 공유 여부
     */
    public void updateSettings(
            boolean autoSaveEnabled,
            boolean formatOnSaveEnabled,
            boolean guestCanEdit,
            boolean shareCursorPosition
    ) {
        this.autoSaveEnabled = autoSaveEnabled;
        this.formatOnSaveEnabled = formatOnSaveEnabled;
        this.guestCanEdit = guestCanEdit;
        this.shareCursorPosition = shareCursorPosition;
    }
}