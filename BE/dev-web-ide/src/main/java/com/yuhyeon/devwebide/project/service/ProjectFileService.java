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
    private Project validateActiveProject(Long projectId) {
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

        return project;
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

    public ProjectFileContentResponse getFileContent(Long projectId, Long fileId) {
        Project project = validateActiveProject(projectId);

        ProjectFile projectFile = projectFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트 파일입니다."));

        validateReadableProjectFile(projectId, projectFile);

        String content = projectFileStorageService.read(project, projectFile);

        return ProjectFileContentResponse.of(projectFile, content);
    }

    @Transactional
    public ProjectFileCreateResponse createFile(
            Long projectId,
            ProjectFileCreateRequest request
    ) {
        Project project = validateActiveProject(projectId);
        ProjectFile parentFile = findParentFile(projectId, request.parentFileId());

        validateFileName(request.name());

        String path = buildPath(parentFile, request.name());
        Long parentFileId = parentFile.getId();

        if (projectFileRepository.existsByProjectIdAndParentFileIdAndNameAndStatus(
                projectId,
                parentFileId,
                request.name(),
                ProjectFileStatus.ACTIVE
        )) {
            throw new IllegalArgumentException("같은 위치에 동일한 이름의 파일 또는 폴더가 이미 존재합니다.");
        }

        ProjectFile projectFile = ProjectFile.builder()
                .project(project)
                .parentFile(parentFile)
                .name(request.name())
                .path(path)
                .fileType(request.fileType())
                .mimeType(resolveMimeType(request.name(), request.fileType()))
                .sizeBytes(0L)
                .status(ProjectFileStatus.ACTIVE)
                .build();

        ProjectFile savedProjectFile = projectFileRepository.save(projectFile);

        if (savedProjectFile.getFileType() == ProjectFileType.FILE) {
            projectFileStorageService.createFile(project, savedProjectFile);
        } else {
            projectFileStorageService.createDirectory(project, savedProjectFile);
        }

        return ProjectFileCreateResponse.from(savedProjectFile);
    }

    @Transactional
    public ProjectFileCreateResponse renameFile(
            Long projectId,
            Long fileId,
            ProjectFileRenameRequest request
    ) {
        Project project = validateActiveProject(projectId);

        ProjectFile projectFile = projectFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트 파일입니다."));

        validateRenamableProjectFile(projectId, projectFile);
        validateFileName(request.name());

        if (projectFile.getName().equals(request.name())) {
            return ProjectFileCreateResponse.from(projectFile);
        }

        Long parentFileId = getParentFileId(projectFile);

        if (projectFileRepository.existsByProjectIdAndParentFileIdAndNameAndStatus(
                projectId,
                parentFileId,
                request.name(),
                ProjectFileStatus.ACTIVE
        )) {
            throw new IllegalArgumentException("같은 위치에 동일한 이름의 파일 또는 폴더가 이미 존재합니다.");
        }

        String oldPath = projectFile.getPath();
        String newPath = buildRenamePath(projectFile, request.name());

        projectFileStorageService.rename(project, projectFile, newPath);

        projectFile.rename(request.name(), newPath);

        if (projectFile.getFileType() == ProjectFileType.FILE) {
            projectFile.updateFileMetadata(
                    resolveMimeType(request.name(), ProjectFileType.FILE),
                    projectFile.getSizeBytes()
            );
        } else {
            renameDescendantPaths(projectId, oldPath, newPath);
        }

        return ProjectFileCreateResponse.from(projectFile);
    }

    @Transactional
    public ProjectFileDeleteResponse deleteFile(Long projectId, Long fileId) {
        Project project = validateActiveProject(projectId);

        ProjectFile projectFile = projectFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트 파일입니다."));

        validateDeletableProjectFile(projectId, projectFile);

        projectFileStorageService.delete(project, projectFile);

        if (projectFile.getFileType() == ProjectFileType.DIRECTORY) {
            deleteDescendants(projectId, projectFile.getPath());
        }

        projectFile.delete();

        return ProjectFileDeleteResponse.from(projectFile);
    }

    private void validateSaveActor(Long userId, Long guestSessionId) {
        boolean hasUserId = userId != null;
        boolean hasGuestSessionId = guestSessionId != null;

        if (hasUserId == hasGuestSessionId) {
            throw new IllegalArgumentException("userId 또는 guestSessionId 중 하나만 전달해야 합니다.");
        }
    }

    private void validateReadableProjectFile(
            Long projectId,
            ProjectFile projectFile
    ) {
        if (!projectFile.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("해당 프로젝트에 속한 파일이 아닙니다.");
        }

        if (projectFile.getStatus() != ProjectFileStatus.ACTIVE) {
            throw new IllegalArgumentException("삭제된 파일은 조회할 수 없습니다.");
        }

        if (projectFile.getFileType() != ProjectFileType.FILE) {
            throw new IllegalArgumentException("디렉터리는 내용을 조회할 수 없습니다.");
        }
    }

    private void validateRenamableProjectFile(
            Long projectId,
            ProjectFile projectFile
    ) {
        if (!projectFile.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("해당 프로젝트에 속한 파일이 아닙니다.");
        }

        if (projectFile.getStatus() != ProjectFileStatus.ACTIVE) {
            throw new IllegalArgumentException("삭제된 파일은 이름을 변경할 수 없습니다.");
        }

        if (projectFile.isRootDirectory()) {
            throw new IllegalArgumentException("루트 디렉터리는 이름을 변경할 수 없습니다.");
        }
    }

    private void validateDeletableProjectFile(
            Long projectId,
            ProjectFile projectFile
    ) {
        if (!projectFile.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("해당 프로젝트에 속한 파일이 아닙니다.");
        }

        if (projectFile.getStatus() != ProjectFileStatus.ACTIVE) {
            throw new IllegalArgumentException("삭제된 파일은 다시 삭제할 수 없습니다.");
        }

        if (projectFile.isRootDirectory()) {
            throw new IllegalArgumentException("루트 디렉터리는 삭제할 수 없습니다.");
        }
    }

    private void renameDescendantPaths(
            Long projectId,
            String oldPath,
            String newPath
    ) {
        List<ProjectFile> descendants =
                projectFileRepository.findByProjectIdAndStatusAndPathStartingWithOrderByPathAsc(
                        projectId,
                        ProjectFileStatus.ACTIVE,
                        oldPath + "/"
                );

        for (ProjectFile descendant : descendants) {
            String descendantNewPath =
                    newPath + descendant.getPath().substring(oldPath.length());

            descendant.move(descendant.getParentFile(), descendantNewPath);
        }
    }

    private void deleteDescendants(
            Long projectId,
            String path
    ) {
        List<ProjectFile> descendants =
                projectFileRepository.findByProjectIdAndStatusAndPathStartingWithOrderByPathAsc(
                        projectId,
                        ProjectFileStatus.ACTIVE,
                        path + "/"
                );

        for (ProjectFile descendant : descendants) {
            descendant.delete();
        }
    }

    private ProjectFile findParentFile(Long projectId, Long parentFileId) {
        if (parentFileId == null) {
            ProjectFile rootDirectory = projectFileRepository.findByProjectIdAndParentFileIsNullAndStatus(
                    projectId,
                    ProjectFileStatus.ACTIVE
            ).orElseThrow(() -> new IllegalArgumentException("프로젝트 루트 디렉터리를 찾을 수 없습니다."));

            validateParentDirectory(projectId, rootDirectory);

            return rootDirectory;
        }

        ProjectFile parentFile = projectFileRepository.findById(parentFileId)
                .orElseThrow(() -> new IllegalArgumentException("부모 파일을 찾을 수 없습니다."));

        validateParentDirectory(projectId, parentFile);

        return parentFile;
    }

    private void validateParentDirectory(
            Long projectId,
            ProjectFile parentFile
    ) {
        if (!parentFile.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("해당 프로젝트에 속한 부모 파일이 아닙니다.");
        }

        if (parentFile.getStatus() != ProjectFileStatus.ACTIVE) {
            throw new IllegalArgumentException("삭제된 폴더에는 파일을 생성할 수 없습니다.");
        }

        if (parentFile.getFileType() != ProjectFileType.DIRECTORY) {
            throw new IllegalArgumentException("디렉터리 하위에만 파일을 생성할 수 있습니다.");
        }
    }

    private void validateFileName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("파일/폴더 이름은 필수입니다.");
        }

        if (name.contains("/") || name.contains("\\") || name.contains("..")) {
            throw new IllegalArgumentException("사용할 수 없는 파일/폴더 이름입니다.");
        }
    }

    private String buildPath(ProjectFile parentFile, String name) {
        if (parentFile.isRootDirectory()) {
            return "/" + name;
        }

        return parentFile.getPath() + "/" + name;
    }

    private String buildRenamePath(ProjectFile projectFile, String name) {
        if (projectFile.getParentFile() == null) {
            return "/" + name;
        }

        return buildPath(projectFile.getParentFile(), name);
    }

    private String resolveMimeType(String name, ProjectFileType fileType) {
        if (fileType == ProjectFileType.DIRECTORY) {
            return null;
        }

        String lowerName = name.toLowerCase();

        if (lowerName.endsWith(".js") || lowerName.endsWith(".jsx")) {
            return "text/javascript";
        }

        if (lowerName.endsWith(".ts") || lowerName.endsWith(".tsx")) {
            return "text/typescript";
        }

        if (lowerName.endsWith(".json")) {
            return "application/json";
        }

        if (lowerName.endsWith(".md")) {
            return "text/markdown";
        }

        if (lowerName.endsWith(".py")) {
            return "text/x-python";
        }

        if (lowerName.endsWith(".java")) {
            return "text/x-java-source";
        }

        if (lowerName.endsWith(".html")) {
            return "text/html";
        }

        if (lowerName.endsWith(".css")) {
            return "text/css";
        }

        return "text/plain";
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
