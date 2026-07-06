package com.yuhyeon.devwebide.project.service;

import com.yuhyeon.devwebide.project.domain.*;
import com.yuhyeon.devwebide.project.dto.*;
import com.yuhyeon.devwebide.project.repository.FileVersionRepository;
import com.yuhyeon.devwebide.project.repository.ProjectFileRepository;
import com.yuhyeon.devwebide.project.repository.ProjectRepository;
import com.yuhyeon.devwebide.project.repository.ProjectSaveBatchRepository;
import com.yuhyeon.devwebide.user.domain.GuestSession;
import com.yuhyeon.devwebide.user.domain.User;
import com.yuhyeon.devwebide.user.repository.GuestSessionRepository;
import com.yuhyeon.devwebide.user.repository.UserRepository;
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
    private final ProjectSaveBatchRepository projectSaveBatchRepository;
    private final FileVersionRepository fileVersionRepository;
    private final UserRepository userRepository;
    private final GuestSessionRepository guestSessionRepository;
    private final ProjectFileStorageService projectFileStorageService;

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

    @Transactional
    public ProjectFileSaveResponse saveFiles(
            Long projectId,
            ProjectFileSaveRequest request
    ) {
        validateSaveActor(request.userId(), request.guestSessionId());

        Project project = projectRepository.findByIdAndStatus(projectId, ProjectStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 삭제된 프로젝트입니다."));

        User user = findUserOrNull(request.userId());
        GuestSession guestSession = findGuestSessionOrNull(request.guestSessionId());

        List<ProjectFile> projectFiles = validateAndFindProjectFiles(projectId, request.files());

        ProjectSaveBatch saveBatch = projectSaveBatchRepository.save(
                ProjectSaveBatch.builder()
                        .project(project)
                        .user(user)
                        .guestSession(guestSession)
                        .status(ProjectSaveBatchStatus.SUCCESS)
                        .savedFileCount(projectFiles.size())
                        .build()
        );

        List<SavedFileResponse> savedFiles = new ArrayList<>();

        for (int i = 0; i < request.files().size(); i++) {
            ProjectFileSaveItemRequest itemRequest = request.files().get(i);
            ProjectFile projectFile = projectFiles.get(i);

            Integer nextVersionNo = getNextVersionNo(projectFile.getId());

            ProjectFileStorageService.StoredFile storedFile =
                    projectFileStorageService.save(
                            project,
                            projectFile,
                            itemRequest.content(),
                            nextVersionNo
                    );

            projectFile.updateFileMetadata(
                    projectFile.getMimeType(),
                    storedFile.sizeBytes()
            );

            FileVersion fileVersion = fileVersionRepository.save(
                    FileVersion.builder()
                            .projectFile(projectFile)
                            .saveBatch(saveBatch)
                            .versionNo(nextVersionNo)
                            .storagePath(storedFile.storagePath())
                            .contentHash(storedFile.contentHash())
                            .sizeBytes(storedFile.sizeBytes())
                            .build()
            );

            savedFiles.add(SavedFileResponse.from(fileVersion));
        }

        return ProjectFileSaveResponse.of(
                project.getId(),
                saveBatch.getId(),
                saveBatch.getStatus(),
                saveBatch.getSavedFileCount(),
                savedFiles
        );
    }

    private void validateSaveActor(Long userId, Long guestSessionId) {
        boolean hasUserId = userId != null;
        boolean hasGuestSessionId = guestSessionId != null;

        if (hasUserId == hasGuestSessionId) {
            throw new IllegalArgumentException("userId 또는 guestSessionId 중 하나만 전달해야 합니다.");
        }
    }

    private User findUserOrNull(Long userId) {
        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    private GuestSession findGuestSessionOrNull(Long guestSessionId) {
        if (guestSessionId == null) {
            return null;
        }

        return guestSessionRepository.findById(guestSessionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게스트 세션입니다."));
    }

    private List<ProjectFile> validateAndFindProjectFiles(
            Long projectId,
            List<ProjectFileSaveItemRequest> files
    ) {
        List<ProjectFile> projectFiles = new ArrayList<>();

        for (ProjectFileSaveItemRequest fileRequest : files) {
            ProjectFile projectFile = projectFileRepository
                    .findById(fileRequest.projectFileId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트 파일입니다."));

            if (!projectFile.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("해당 프로젝트에 속한 파일이 아닙니다.");
            }

            if (projectFile.getStatus() != ProjectFileStatus.ACTIVE) {
                throw new IllegalArgumentException("삭제된 파일은 저장할 수 없습니다.");
            }

            if (projectFile.getFileType() != ProjectFileType.FILE) {
                throw new IllegalArgumentException("디렉토리는 저장할 수 없습니다.");
            }

            projectFiles.add(projectFile);
        }

        return projectFiles;
    }

    private Integer getNextVersionNo(Long projectFileId) {
        return fileVersionRepository.findTopByProjectFileIdOrderByVersionNoDesc(projectFileId)
                .map(fileVersion -> fileVersion.getVersionNo() + 1)
                .orElse(1);
    }
}