package com.yuhyeon.devwebide.project.service;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectFile;

/**
 * 프로젝트 파일 원본 저장소 인터페이스
 *
 * 실제 파일 원본은 DB가 아니라 EFS에 저장한다.
 * ProjectFileService는 저장 흐름만 담당하고,
 * 실제 파일 쓰기/해시 계산/저장 경로 생성은 구현체에 위임한다.
 */
public interface ProjectFileStorageService {

    /**
     * 프로젝트 파일 원본을 저장한다.
     *
     * @param project 저장 대상 프로젝트
     * @param projectFile 저장 대상 파일 메타데이터
     * @param content 저장할 파일 내용
     * @param versionNo 생성할 파일 버전 번호
     * @return 저장 결과
     */
    StoredFile save(
            Project project,
            ProjectFile projectFile,
            String content,
            Integer versionNo
    );

    /**
     * 파일 저장 결과
     *
     * FileVersion 생성에 필요한 값을 담는다.
     *
     * @param storagePath 실제 저장된 파일 경로
     * @param contentHash 파일 내용 해시
     * @param sizeBytes 저장된 파일 크기
     */
    record StoredFile(
            String storagePath,
            String contentHash,
            Long sizeBytes
    ) {
    }
}