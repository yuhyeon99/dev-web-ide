package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 파일 버전 Repository
 *
 * 프로젝트 파일 저장 시 생성되는 파일 버전 메타데이터를 조회합니다.
 * 파일별 버전 이력 조회, 최신 버전 조회, 저장 배치별 저장 파일 목록 조회에 사용합니다.
 */
public interface FileVersionRepository extends JpaRepository<FileVersion, Long> {

    /**
     * 프로젝트 파일 기준 버전 목록 조회
     *
     * @param projectFileId 프로젝트 파일 ID
     * @return 버전 번호 내림차순 파일 버전 목록
     */
    List<FileVersion> findByProjectFileIdOrderByVersionNoDesc(Long projectFileId);

    /**
     * 프로젝트 파일 기준 최신 버전 조회
     *
     * @param projectFileId 프로젝트 파일 ID
     * @return 가장 최신 파일 버전
     */
    Optional<FileVersion> findTopByProjectFileIdOrderByVersionNoDesc(Long projectFileId);

    /**
     * 프로젝트 파일 ID와 버전 번호 기준 파일 버전 조회
     *
     * @param projectFileId 프로젝트 파일 ID
     * @param versionNo 버전 번호
     * @return 파일 버전
     */
    Optional<FileVersion> findByProjectFileIdAndVersionNo(
            Long projectFileId,
            Integer versionNo
    );

    /**
     * 프로젝트 파일 ID와 버전 번호 기준 파일 버전 존재 여부 확인
     *
     * @param projectFileId 프로젝트 파일 ID
     * @param versionNo 버전 번호
     * @return 존재 여부
     */
    boolean existsByProjectFileIdAndVersionNo(
            Long projectFileId,
            Integer versionNo
    );

    /**
     * 저장 배치 기준 파일 버전 목록 조회
     *
     * @param saveBatchId 저장 배치 ID
     * @return 생성 순 파일 버전 목록
     */
    List<FileVersion> findBySaveBatchIdOrderByCreatedAtAsc(Long saveBatchId);

    /**
     * 저장 배치 기준 파일 버전 개수 조회
     *
     * @param saveBatchId 저장 배치 ID
     * @return 저장 배치에 포함된 파일 버전 수
     */
    long countBySaveBatchId(Long saveBatchId);

    /**
     * 프로젝트 파일 기준 파일 버전 개수 조회
     *
     * @param projectFileId 프로젝트 파일 ID
     * @return 프로젝트 파일의 버전 수
     */
    long countByProjectFileId(Long projectFileId);
}