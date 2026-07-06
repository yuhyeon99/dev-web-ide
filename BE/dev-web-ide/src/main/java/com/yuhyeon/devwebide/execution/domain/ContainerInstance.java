package com.yuhyeon.devwebide.execution.domain;

import com.yuhyeon.devwebide.workspace.domain.WorkspaceSession;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 컨테이너 인스턴스 엔티티
 *
 * 프로젝트 실행 시 생성되는 실제 컨테이너 실행 정보를 관리합니다.
 * ECS/Fargate 또는 로컬 Docker 컨테이너의 실행 상태, 이미지, 식별자,
 * EFS 마운트 경로 등을 저장합니다.
 */
@Entity
@Table(name = "container_instances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ContainerInstance {

    /**
     * 컨테이너 인스턴스 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 연결된 워크스페이스 세션
     *
     * 하나의 워크스페이스 실행 세션은 여러 컨테이너 실행 기록을 가질 수 있습니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_session_id", nullable = false)
    private WorkspaceSession workspaceSession;

    /**
     * 컨테이너 실행 제공자
     *
     * 예:
     * LOCAL
     * ECS
     * ECS_FARGATE
     */
    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    /**
     * ECS Task ARN
     *
     * ECS/Fargate 실행 시 사용합니다.
     * 로컬 Docker 실행 시 null일 수 있습니다.
     */
    @Column(name = "task_arn", length = 500)
    private String taskArn;

    /**
     * 컨테이너 ID
     *
     * Docker 컨테이너 ID 또는 ECS 컨테이너 식별자를 저장합니다.
     */
    @Column(name = "container_id", length = 200)
    private String containerId;

    /**
     * 실행에 사용한 Docker 이미지
     *
     * 예:
     * node:20
     * python:3.12
     * eclipse-temurin:21
     */
    @Column(name = "docker_image", nullable = false, length = 200)
    private String dockerImage;

    /**
     * 컨테이너 실행 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ContainerInstanceStatus status;

    /**
     * EFS 마운트 경로
     *
     * 컨테이너 내부에서 프로젝트 파일에 접근하는 경로입니다.
     *
     * 예:
     * /workspace
     * /mnt/efs/projects/{projectId}
     */
    @Column(name = "efs_mount_path", length = 500)
    private String efsMountPath;

    /**
     * 컨테이너 실행 정보 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 컨테이너 종료 일시
     */
    @Column(name = "stopped_at")
    private LocalDateTime stoppedAt;

    /**
     * ContainerInstance 생성자
     *
     * @param workspaceSession 워크스페이스 세션
     * @param provider 컨테이너 실행 제공자
     * @param taskArn ECS Task ARN
     * @param containerId 컨테이너 ID
     * @param dockerImage Docker 이미지
     * @param status 컨테이너 상태
     * @param efsMountPath EFS 마운트 경로
     */
    @Builder
    public ContainerInstance(
            WorkspaceSession workspaceSession,
            String provider,
            String taskArn,
            String containerId,
            String dockerImage,
            ContainerInstanceStatus status,
            String efsMountPath
    ) {
        validateRequiredFields(
                workspaceSession,
                provider,
                dockerImage,
                status
        );

        this.workspaceSession = workspaceSession;
        this.provider = provider;
        this.taskArn = taskArn;
        this.containerId = containerId;
        this.dockerImage = dockerImage;
        this.status = status;
        this.efsMountPath = efsMountPath;
    }

    /**
     * 필수 값 검증
     *
     * @param workspaceSession 워크스페이스 세션
     * @param provider 실행 제공자
     * @param dockerImage Docker 이미지
     * @param status 컨테이너 상태
     */
    private void validateRequiredFields(
            WorkspaceSession workspaceSession,
            String provider,
            String dockerImage,
            ContainerInstanceStatus status
    ) {
        if (workspaceSession == null) {
            throw new IllegalArgumentException("워크스페이스 세션은 필수입니다.");
        }

        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("컨테이너 실행 제공자는 필수입니다.");
        }

        if (dockerImage == null || dockerImage.isBlank()) {
            throw new IllegalArgumentException("Docker 이미지는 필수입니다.");
        }

        if (status == null) {
            throw new IllegalArgumentException("컨테이너 상태는 필수입니다.");
        }
    }

    /**
     * 컨테이너 시작 중 여부 확인
     *
     * @return 시작 중 여부
     */
    public boolean isStarting() {
        return this.status == ContainerInstanceStatus.STARTING;
    }

    /**
     * 컨테이너 실행 중 여부 확인
     *
     * @return 실행 중 여부
     */
    public boolean isRunning() {
        return this.status == ContainerInstanceStatus.RUNNING;
    }

    /**
     * 컨테이너 종료 여부 확인
     *
     * @return 종료 여부
     */
    public boolean isStopped() {
        return this.status == ContainerInstanceStatus.STOPPED;
    }

    /**
     * 컨테이너 실패 여부 확인
     *
     * @return 실패 여부
     */
    public boolean isFailed() {
        return this.status == ContainerInstanceStatus.FAILED;
    }

    /**
     * 컨테이너 실행 완료 처리
     *
     * 실제 컨테이너 ID 또는 ECS Task ARN이 확인된 뒤 호출합니다.
     *
     * @param taskArn ECS Task ARN
     * @param containerId 컨테이너 ID
     */
    public void markRunning(String taskArn, String containerId) {
        this.taskArn = taskArn;
        this.containerId = containerId;
        this.status = ContainerInstanceStatus.RUNNING;
    }

    /**
     * 컨테이너 종료 처리
     *
     * @param stoppedAt 종료 일시
     */
    public void stop(LocalDateTime stoppedAt) {
        this.status = ContainerInstanceStatus.STOPPED;
        this.stoppedAt = stoppedAt;
    }

    /**
     * 컨테이너 실패 처리
     *
     * @param stoppedAt 실패 일시
     */
    public void fail(LocalDateTime stoppedAt) {
        this.status = ContainerInstanceStatus.FAILED;
        this.stoppedAt = stoppedAt;
    }
}