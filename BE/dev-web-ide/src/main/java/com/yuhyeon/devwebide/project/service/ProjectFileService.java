package com.yuhyeon.devwebide.project.service;

import com.yuhyeon.devwebide.project.domain.Project;
import com.yuhyeon.devwebide.project.domain.ProjectFile;
import com.yuhyeon.devwebide.project.domain.ProjectFileStatus;
import com.yuhyeon.devwebide.project.domain.ProjectStatus;
import com.yuhyeon.devwebide.project.dto.ProjectFileTreeResponse;
import com.yuhyeon.devwebide.project.repository.ProjectFileRepository;
import com.yuhyeon.devwebide.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 프로젝트 파일 서비스
 *
 * 프로젝트 파일/폴더 트리 조회를 처리합니다.
 * 실제 파일 원본은 EFS에 저장하고,
 * 이 서비스는 PROJECT_FILES 테이블의 메타데이터를 기준으로 트리를 구성합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectFileService {

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;

    /**
     * 프로젝트 파일 트리 조회
     *
     * ACTIVE 상태의 프로젝트인지 확인한 뒤,
     * ACTIVE 상태의 파일/폴더 목록을 조회하여 부모-자식 트리 구조로 변환합니다.
     *
     * @param projectId 프로젝트 ID
     * @return 파일 트리 목록
     */
    public List<ProjectFileTreeResponse> getFileTree(Long projectId) {
        validateActiveProject(projectId);

        List<ProjectFile> projectFiles =
                projectFileRepository.findByProjectIdAndStatusOrderByPathAsc(
                        projectId,
                        ProjectFileStatus.ACTIVE
                );

        Map<Long, List<ProjectFile>> childrenByParentId =
                groupByParentFileId(projectFiles);

        List<ProjectFile> rootFiles =
                childrenByParentId.getOrDefault(null, List.of());

        return rootFiles.stream()
                .map(rootFile -> toTreeResponse(rootFile, childrenByParentId))
                .toList();
    }

    /**
     * ACTIVE 상태의 프로젝트인지 확인합니다.
     *
     * 존재하지 않거나 삭제된 프로젝트는 파일 트리를 조회할 수 없습니다.
     *
     * @param projectId 프로젝트 ID
     */
    private void validateActiveProject(Long projectId) {
        Project project = projectRepository.findByIdAndStatus(
                projectId,
                ProjectStatus.ACTIVE
        ).orElseThrow(() -> new IllegalArgumentException(
                "프로젝트를 찾을 수 없습니다. projectId=" + projectId
        ));

        if (!project.isActive()) {
            throw new IllegalArgumentException(
                    "활성 프로젝트만 파일 트리를 조회할 수 있습니다. projectId=" + projectId
            );
        }
    }

    /**
     * parentFileId 기준으로 파일/폴더 목록을 그룹화합니다.
     *
     * 루트 디렉토리는 parentFile이 없으므로 null key에 저장합니다.
     *
     * @param projectFiles 파일/폴더 목록
     * @return parentFileId 기준 그룹
     */
    private Map<Long, List<ProjectFile>> groupByParentFileId(
            List<ProjectFile> projectFiles
    ) {
        Map<Long, List<ProjectFile>> childrenByParentId = new LinkedHashMap<>();

        for (ProjectFile projectFile : projectFiles) {
            Long parentFileId = getParentFileId(projectFile);

            childrenByParentId
                    .computeIfAbsent(parentFileId, key -> new ArrayList<>())
                    .add(projectFile);
        }

        return childrenByParentId;
    }

    /**
     * ProjectFile 엔티티를 재귀적으로 트리 응답 DTO로 변환합니다.
     *
     * @param projectFile 변환 대상 파일/폴더
     * @param childrenByParentId parentFileId 기준 그룹
     * @return 파일 트리 응답 DTO
     */
    private ProjectFileTreeResponse toTreeResponse(
            ProjectFile projectFile,
            Map<Long, List<ProjectFile>> childrenByParentId
    ) {
        List<ProjectFile> children =
                childrenByParentId.getOrDefault(projectFile.getId(), List.of());

        List<ProjectFileTreeResponse> childResponses = children.stream()
                .map(child -> toTreeResponse(child, childrenByParentId))
                .toList();

        return ProjectFileTreeResponse.from(projectFile, childResponses);
    }

    /**
     * 부모 파일 ID를 반환합니다.
     *
     * @param projectFile 파일/폴더 엔티티
     * @return 부모 파일 ID
     */
    private Long getParentFileId(ProjectFile projectFile) {
        if (projectFile.getParentFile() == null) {
            return null;
        }

        return projectFile.getParentFile().getId();
    }
}