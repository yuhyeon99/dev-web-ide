package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.ProjectFile;
import com.yuhyeon.devwebide.project.domain.ProjectFileStatus;
import com.yuhyeon.devwebide.project.domain.ProjectFileType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {

    /**
     * 프로젝트 ID와 경로로 파일/폴더 조회
     *
     * 파일 내용 조회, 파일명 변경, 파일/폴더 삭제 시
     * 대상 파일/폴더를 찾는 데 사용합니다.
     */
    Optional<ProjectFile> findByProjectIdAndPath(
            Long projectId,
            String path
    );

    /**
     * 프로젝트 ID, 경로, 상태로 파일/폴더 조회
     *
     * 삭제 처리되지 않은 ACTIVE 상태의 파일/폴더만 조회할 때 사용합니다.
     */
    Optional<ProjectFile> findByProjectIdAndPathAndStatus(
            Long projectId,
            String path,
            ProjectFileStatus status
    );

    /**
     * 프로젝트 ID와 경로로 파일/폴더 존재 여부 확인
     *
     * 같은 프로젝트 안에서 동일 경로의 파일/폴더가
     * 중복 생성되지 않도록 검증할 때 사용합니다.
     */
    boolean existsByProjectIdAndPath(
            Long projectId,
            String path
    );

    /**
     * 프로젝트 ID, 경로, 상태로 파일/폴더 존재 여부 확인
     *
     * ACTIVE 상태 기준 중복 경로를 확인할 때 사용합니다.
     */
    boolean existsByProjectIdAndPathAndStatus(
            Long projectId,
            String path,
            ProjectFileStatus status
    );

    /**
     * 프로젝트의 파일/폴더 트리 전체 조회
     *
     * 파일 트리 조회 API에서 사용합니다.
     * path 기준으로 정렬하면 FE에서 트리 구조를 구성하기 쉽습니다.
     */
    List<ProjectFile> findByProjectIdAndStatusOrderByPathAsc(
            Long projectId,
            ProjectFileStatus status
    );

    /**
     * 특정 부모 폴더의 하위 파일/폴더 목록 조회
     *
     * 특정 폴더를 펼쳤을 때 하위 항목을 조회하는 데 사용합니다.
     */
    List<ProjectFile> findByProjectIdAndParentFileIdAndStatusOrderByNameAsc(
            Long projectId,
            Long parentFileId,
            ProjectFileStatus status
    );

    /**
     * 프로젝트의 루트 디렉토리 조회
     *
     * 프로젝트 생성 직후 루트 디렉토리 존재 여부를 확인하거나,
     * 파일 트리의 시작점을 조회할 때 사용합니다.
     */
    Optional<ProjectFile> findByProjectIdAndParentFileIsNullAndStatus(
            Long projectId,
            ProjectFileStatus status
    );

    /**
     * 프로젝트 내 특정 타입의 파일/폴더 목록 조회
     *
     * 파일만 조회하거나 폴더만 조회할 때 사용합니다.
     */
    List<ProjectFile> findByProjectIdAndFileTypeAndStatusOrderByPathAsc(
            Long projectId,
            ProjectFileType fileType,
            ProjectFileStatus status
    );

    /**
     * 특정 부모 폴더 아래 같은 이름의 활성 파일/폴더 존재 여부 확인
     *
     * 동일 폴더 내 중복 파일명 생성을 막을 때 사용합니다.
     */
    boolean existsByProjectIdAndParentFileIdAndNameAndStatus(
            Long projectId,
            Long parentFileId,
            String name,
            ProjectFileStatus status
    );
}