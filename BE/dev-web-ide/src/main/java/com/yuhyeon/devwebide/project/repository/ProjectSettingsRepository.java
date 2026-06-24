package com.yuhyeon.devwebide.project.repository;

import com.yuhyeon.devwebide.project.domain.ProjectSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectSettingsRepository extends JpaRepository<ProjectSettings, Long> {

    /**
     * 프로젝트 ID로 프로젝트 설정 조회
     *
     * 프로젝트 하나당 설정은 하나만 존재하므로 Optional로 반환합니다.
     */
    Optional<ProjectSettings> findByProjectId(Long projectId);

    /**
     * 프로젝트 ID 기준 설정 존재 여부 확인
     *
     * 프로젝트 생성 시 기본 설정이 이미 생성되어 있는지 확인할 때 사용할 수 있습니다.
     */
    boolean existsByProjectId(Long projectId);
}