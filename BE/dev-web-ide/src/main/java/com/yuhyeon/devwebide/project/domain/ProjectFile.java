package com.yuhyeon.devwebide.project.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 프로젝트 파일 엔티티
 *
 * 웹 IDE 프로젝트의 파일/폴더 트리 메타데이터를 관리합니다.
 * 실제 파일 원본은 EFS에 저장하고,
 * DB에는 파일명, 경로, 타입, 크기, 상태 같은 메타데이터만 저장합니다.
 */
@Entity
@Table(
        name = "project_files",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_file_path",
                        columnNames = {"project_id", "path"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ProjectFile {

    /**
     * 프로젝트 파일 PK
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 파일/폴더가 속한 프로젝트
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * 부모 파일/폴더
     *
     * 루트 디렉토리인 경우 null입니다.
     * 일반 파일 또는 하위 폴더는 부모 폴더를 가집니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_file_id")
    private ProjectFile parentFile;

    /**
     * 파일/폴더명
     *
     * 예: src, App.jsx, package.json
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 프로젝트 내부 경로
     *
     * 예:
     * /
     * /src
     * /src/App.jsx
     * /package.json
     */
    @Column(name = "path", nullable = false, length = 1000)
    private String path;

    /**
     * 파일 타입
     *
     * FILE, DIRECTORY
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 30)
    private ProjectFileType fileType;

    /**
     * MIME 타입
     *
     * 파일인 경우 사용합니다.
     * 폴더인 경우 null일 수 있습니다.
     *
     * 예: text/plain, application/json, text/javascript
     */
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /**
     * 파일 크기
     *
     * 폴더인 경우 0으로 저장합니다.
     */
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    /**
     * 파일 상태
     *
     * ACTIVE, DELETED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProjectFileStatus status;

    /**
     * 생성 일시
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * ProjectFile 생성자
     *
     * @param project 파일/폴더가 속한 프로젝트
     * @param parentFile 부모 파일/폴더
     * @param name 파일/폴더명
     * @param path 프로젝트 내부 경로
     * @param fileType 파일 타입
     * @param mimeType MIME 타입
     * @param sizeBytes 파일 크기
     * @param status 파일 상태
     */
    @Builder
    public ProjectFile(
            Project project,
            ProjectFile parentFile,
            String name,
            String path,
            ProjectFileType fileType,
            String mimeType,
            Long sizeBytes,
            ProjectFileStatus status
    ) {
        validateRequiredFields(project, name, path, fileType, sizeBytes, status);
        validateDirectoryMimeType(fileType, mimeType);
        validateDirectorySize(fileType, sizeBytes);

        this.project = project;
        this.parentFile = parentFile;
        this.name = name;
        this.path = path;
        this.fileType = fileType;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.status = status;
    }

    /**
     * 필수 값 검증
     *
     * @param project 프로젝트
     * @param name 파일/폴더명
     * @param path 프로젝트 내부 경로
     * @param fileType 파일 타입
     * @param sizeBytes 파일 크기
     * @param status 파일 상태
     */
    private void validateRequiredFields(
            Project project,
            String name,
            String path,
            ProjectFileType fileType,
            Long sizeBytes,
            ProjectFileStatus status
    ) {
        if (project == null) {
            throw new IllegalArgumentException("프로젝트는 필수입니다.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("파일/폴더명은 필수입니다.");
        }

        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("파일 경로는 필수입니다.");
        }

        if (fileType == null) {
            throw new IllegalArgumentException("파일 타입은 필수입니다.");
        }

        if (sizeBytes == null || sizeBytes < 0) {
            throw new IllegalArgumentException("파일 크기는 0 이상이어야 합니다.");
        }

        if (status == null) {
            throw new IllegalArgumentException("파일 상태는 필수입니다.");
        }
    }

    /**
     * 폴더 MIME 타입 검증
     *
     * 폴더는 실제 파일이 아니므로 mimeType을 가지지 않습니다.
     *
     * @param fileType 파일 타입
     * @param mimeType MIME 타입
     */
    private void validateDirectoryMimeType(
            ProjectFileType fileType,
            String mimeType
    ) {
        if (fileType == ProjectFileType.DIRECTORY && mimeType != null) {
            throw new IllegalArgumentException(
                    "폴더는 MIME 타입을 가질 수 없습니다."
            );
        }
    }

    /**
     * 폴더 크기 검증
     *
     * 폴더는 파일 내용이 없으므로 sizeBytes를 0으로 저장합니다.
     *
     * @param fileType 파일 타입
     * @param sizeBytes 파일 크기
     */
    private void validateDirectorySize(
            ProjectFileType fileType,
            Long sizeBytes
    ) {
        if (fileType == ProjectFileType.DIRECTORY && sizeBytes != 0) {
            throw new IllegalArgumentException(
                    "폴더의 파일 크기는 0이어야 합니다."
            );
        }
    }

    /**
     * 프로젝트 루트 디렉토리 생성
     *
     * 프로젝트 생성 시 기본 루트 디렉토리 메타데이터를 생성할 때 사용합니다.
     *
     * @param project 프로젝트
     * @return 루트 디렉토리
     */
    public static ProjectFile createRootDirectory(Project project) {
        return ProjectFile.builder()
                .project(project)
                .parentFile(null)
                .name("/")
                .path("/")
                .fileType(ProjectFileType.DIRECTORY)
                .mimeType(null)
                .sizeBytes(0L)
                .status(ProjectFileStatus.ACTIVE)
                .build();
    }

    /**
     * 파일 여부 확인
     *
     * @return 파일 여부
     */
    public boolean isFile() {
        return this.fileType == ProjectFileType.FILE;
    }

    /**
     * 폴더 여부 확인
     *
     * @return 폴더 여부
     */
    public boolean isDirectory() {
        return this.fileType == ProjectFileType.DIRECTORY;
    }

    /**
     * 루트 디렉토리 여부 확인
     *
     * @return 루트 디렉토리 여부
     */
    public boolean isRootDirectory() {
        return this.parentFile == null && "/".equals(this.path);
    }

    /**
     * 활성 파일/폴더 여부 확인
     *
     * @return 활성 여부
     */
    public boolean isActive() {
        return this.status == ProjectFileStatus.ACTIVE;
    }

    /**
     * 삭제 처리 여부 확인
     *
     * @return 삭제 여부
     */
    public boolean isDeleted() {
        return this.status == ProjectFileStatus.DELETED;
    }

    /**
     * 파일/폴더 이름 변경
     *
     * 실제 EFS 파일명 변경 후 DB 메타데이터를 갱신할 때 사용합니다.
     *
     * @param name 변경할 파일/폴더명
     * @param path 변경 후 경로
     */
    public void rename(String name, String path) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("파일/폴더명은 필수입니다.");
        }

        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("파일 경로는 필수입니다.");
        }

        this.name = name;
        this.path = path;
    }

    /**
     * 파일/폴더 이동
     *
     * 실제 EFS 파일 이동 후 DB 메타데이터를 갱신할 때 사용합니다.
     *
     * @param parentFile 변경할 부모 폴더
     * @param path 변경 후 경로
     */
    public void move(ProjectFile parentFile, String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("파일 경로는 필수입니다.");
        }

        this.parentFile = parentFile;
        this.path = path;
    }

    /**
     * 파일 메타데이터 변경
     *
     * 파일 저장 후 크기와 MIME 타입을 갱신할 때 사용합니다.
     *
     * @param mimeType MIME 타입
     * @param sizeBytes 파일 크기
     */
    public void updateFileMetadata(String mimeType, Long sizeBytes) {
        if (this.fileType != ProjectFileType.FILE) {
            throw new IllegalStateException(
                    "파일 메타데이터 변경은 FILE 타입에서만 가능합니다."
            );
        }

        if (sizeBytes == null || sizeBytes < 0) {
            throw new IllegalArgumentException("파일 크기는 0 이상이어야 합니다.");
        }

        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
    }

    /**
     * 파일/폴더 삭제 처리
     *
     * 실제 DB row를 삭제하지 않고 상태만 DELETED로 변경합니다.
     */
    public void delete() {
        this.status = ProjectFileStatus.DELETED;
    }

    /**
     * 파일/폴더 복구 처리
     */
    public void restore() {
        this.status = ProjectFileStatus.ACTIVE;
    }
}