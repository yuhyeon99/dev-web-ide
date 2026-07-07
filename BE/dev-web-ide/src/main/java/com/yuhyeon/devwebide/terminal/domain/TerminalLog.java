package com.yuhyeon.devwebide.terminal.domain;

import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 터미널 로그 엔티티
 *
 * 워크스페이스 실행 세션에서 발생한 stdout, stderr, system 로그를 저장합니다.
 */
@Entity
@Table(name = "terminal_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TerminalLog {

    /**
     * 터미널 로그 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 로그가 발생한 워크스페이스 실행 세션
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_session_id", nullable = false)
    private WorkspaceSession workspaceSession;

    /**
     * 세션 내 로그 순번
     */
    @Column(name = "sequence_no", nullable = false)
    private Long sequenceNo;

    /**
     * 터미널 로그 스트림 타입
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "stream_type", nullable = false, length = 30)
    private TerminalStreamType streamType;

    /**
     * 로그 내용
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 로그 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * TerminalLog 생성자
     *
     * @param workspaceSession 워크스페이스 실행 세션
     * @param sequenceNo 세션 내 로그 순번
     * @param streamType 로그 스트림 타입
     * @param content 로그 내용
     */
    @Builder
    public TerminalLog(
            WorkspaceSession workspaceSession,
            Long sequenceNo,
            TerminalStreamType streamType,
            String content
    ) {
        validateRequiredFields(
                workspaceSession,
                sequenceNo,
                streamType,
                content
        );

        this.workspaceSession = workspaceSession;
        this.sequenceNo = sequenceNo;
        this.streamType = streamType;
        this.content = content;
    }

    /**
     * 필수 값 검증
     *
     * @param workspaceSession 워크스페이스 실행 세션
     * @param sequenceNo 로그 순번
     * @param streamType 로그 스트림 타입
     * @param content 로그 내용
     */
    private void validateRequiredFields(
            WorkspaceSession workspaceSession,
            Long sequenceNo,
            TerminalStreamType streamType,
            String content
    ) {
        if (workspaceSession == null) {
            throw new IllegalArgumentException("워크스페이스 세션은 필수입니다.");
        }

        if (sequenceNo == null || sequenceNo < 1) {
            throw new IllegalArgumentException("터미널 로그 순번은 1 이상이어야 합니다.");
        }

        if (streamType == null) {
            throw new IllegalArgumentException("터미널 로그 스트림 타입은 필수입니다.");
        }

        if (content == null) {
            throw new IllegalArgumentException("터미널 로그 내용은 필수입니다.");
        }
    }
}
