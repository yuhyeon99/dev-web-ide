package com.yuhyeon.devwebide.project.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 파일 버전 엔티티
 *
 * 프로젝트 파일 저장 시점의 버전 메타데이터를 관리합니다.
 * 실제 파일 원본 또는 스냅샷은 EFS에 저장하고,
 * DB에는 버전 번호, 저장 경로, 내용 해시, 파일 크기 같은 메타데이터만 저장합니다.
 */
@Entity
@Table(name = "file_versions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class FileVersion {

    /**
     * 파일 버전 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 버전이 생성된 프로젝트 파일
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_file_id", nullable = false)
    private ProjectFile projectFile;

    /**
     * 파일 저장 배치
     *
     * 파일 저장 API에서 여러 dirty 파일을 일괄 저장할 때
     * 같은 저장 요청 단위로 묶기 위해 사용합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "save_batch_id")
    private ProjectSaveBatch saveBatch;

    /**
     * 파일 버전 번호
     *
     * 같은 파일 안에서 저장 순서를 구분합니다.
     * 예: 1, 2, 3
     */
    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    /**
     * 파일 버전 저장 경로
     *
     * 실제 파일 스냅샷이 저장된 EFS 경로입니다.
     * 예: /projects/{projectId}/.versions/{fileId}/v1
     */
    @Column(name = "storage_path", nullable = false, length = 1000)
    private String storagePath;

    /**
     * 파일 내용 해시
     *
     * 동일 내용 여부 확인, 저장 무결성 검증 등에 사용합니다.
     */
    @Column(name = "content_hash", nullable = false, length = 200)
    private String contentHash;

    /**
     * 파일 크기
     */
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    /**
     * 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * FileVersion 생성자
     *
     * @param projectFile 버전이 생성된 프로젝트 파일
     * @param saveBatch 저장 배치
     * @param versionNo 파일 버전 번호
     * @param storagePath 파일 버전 저장 경로
     * @param contentHash 파일 내용 해시
     * @param sizeBytes 파일 크기
     */
    @Builder
    public FileVersion(
            ProjectFile projectFile,
            ProjectSaveBatch saveBatch,
            Integer versionNo,
            String storagePath,
            String contentHash,
            Long sizeBytes
    ) {
        validateRequiredFields(
                projectFile,
                versionNo,
                storagePath,
                contentHash,
                sizeBytes
        );
        validateProjectFileType(projectFile);

        this.projectFile = projectFile;
        this.saveBatch = saveBatch;
        this.versionNo = versionNo;
        this.storagePath = storagePath;
        this.contentHash = contentHash;
        this.sizeBytes = sizeBytes;
    }

    /**
     * 필수 값 검증
     *
     * @param projectFile 버전이 생성된 프로젝트 파일
     * @param versionNo 파일 버전 번호
     * @param storagePath 파일 버전 저장 경로
     * @param contentHash 파일 내용 해시
     * @param sizeBytes 파일 크기
     */
    private void validateRequiredFields(
            ProjectFile projectFile,
            Integer versionNo,
            String storagePath,
            String contentHash,
            Long sizeBytes
    ) {
        if (projectFile == null) {
            throw new IllegalArgumentException("프로젝트 파일은 필수입니다.");
        }

        if (versionNo == null || versionNo <= 0) {
            throw new IllegalArgumentException("파일 버전 번호는 1 이상이어야 합니다.");
        }

        if (storagePath == null || storagePath.isBlank()) {
            throw new IllegalArgumentException("파일 버전 저장 경로는 필수입니다.");
        }

        if (contentHash == null || contentHash.isBlank()) {
            throw new IllegalArgumentException("파일 내용 해시는 필수입니다.");
        }

        if (sizeBytes == null || sizeBytes < 0) {
            throw new IllegalArgumentException("파일 크기는 0 이상이어야 합니다.");
        }
    }

    /**
     * 프로젝트 파일 타입 검증
     *
     * 파일 버전은 DIRECTORY가 아니라 FILE 타입에 대해서만 생성합니다.
     *
     * @param projectFile 버전이 생성된 프로젝트 파일
     */
    private void validateProjectFileType(ProjectFile projectFile) {
        if (!projectFile.isFile()) {
            throw new IllegalArgumentException(
                    "파일 버전은 FILE 타입에 대해서만 생성할 수 있습니다."
            );
        }
    }

    /**
     * 특정 저장 배치에 포함된 버전인지 확인
     *
     * @return 저장 배치 포함 여부
     */
    public boolean hasSaveBatch() {
        return this.saveBatch != null;
    }
}