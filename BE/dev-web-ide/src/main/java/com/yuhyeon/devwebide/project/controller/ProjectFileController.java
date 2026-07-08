package com.yuhyeon.devwebide.project.controller;

import com.yuhyeon.devwebide.project.dto.ProjectFileCreateRequest;
import com.yuhyeon.devwebide.project.dto.ProjectFileCreateResponse;
import com.yuhyeon.devwebide.project.dto.ProjectFileDeleteResponse;
import com.yuhyeon.devwebide.project.dto.ProjectFileRenameRequest;
import com.yuhyeon.devwebide.project.dto.ProjectFileSaveRequest;
import com.yuhyeon.devwebide.project.dto.ProjectFileSaveResponse;
import com.yuhyeon.devwebide.project.dto.ProjectFileContentResponse;
import com.yuhyeon.devwebide.project.dto.ProjectFileTreeResponse;
import jakarta.validation.Valid;
import com.yuhyeon.devwebide.project.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 프로젝트 파일 컨트롤러
 *
 * 웹 IDE 프로젝트의 파일/폴더 관련 API를 처리합니다.
 * 파일 트리 조회, 파일 생성, 파일 내용 조회, 파일 저장,
 * 파일명 변경, 파일/폴더 삭제 API가 이 컨트롤러에 확장될 수 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectFileController {

    private final ProjectFileService projectFileService;

    /**
     * 프로젝트 파일 트리 조회
     *
     * 워크스페이스 진입 후 FE 좌측 파일 탐색기에 표시할
     * 파일/폴더 트리 메타데이터를 조회합니다.
     *
     * @param projectId 프로젝트 ID
     * @return 파일 트리 목록
     */
    @GetMapping("/{projectId}/files/tree")
    public ResponseEntity<List<ProjectFileTreeResponse>> getFileTree(
            @PathVariable Long projectId
    ) {
        List<ProjectFileTreeResponse> response =
                projectFileService.getFileTree(projectId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}/files/{fileId}/content")
    public ResponseEntity<ProjectFileContentResponse> getFileContent(
            @PathVariable Long projectId,
            @PathVariable Long fileId
    ) {
        ProjectFileContentResponse response =
                projectFileService.getFileContent(projectId, fileId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{projectId}/files")
    public ResponseEntity<ProjectFileCreateResponse> createFile(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectFileCreateRequest request
    ) {
        ProjectFileCreateResponse response =
                projectFileService.createFile(projectId, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{projectId}/files/{fileId}/rename")
    public ResponseEntity<ProjectFileCreateResponse> renameFile(
            @PathVariable Long projectId,
            @PathVariable Long fileId,
            @Valid @RequestBody ProjectFileRenameRequest request
    ) {
        ProjectFileCreateResponse response =
                projectFileService.renameFile(projectId, fileId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}/files/{fileId}")
    public ResponseEntity<ProjectFileDeleteResponse> deleteFile(
            @PathVariable Long projectId,
            @PathVariable Long fileId
    ) {
        ProjectFileDeleteResponse response =
                projectFileService.deleteFile(projectId, fileId);

        return ResponseEntity.ok(response);
    }

    /**
     * 프로젝트 파일 저장
     *
     * FE 에디터에서 열린 dirty 파일들을 일괄 저장합니다.
     *
     * @param projectId 프로젝트 ID
     * @param request 파일 저장 요청
     * @return 파일 저장 결과
     */
    @PostMapping("/{projectId}/save")
    public ResponseEntity<ProjectFileSaveResponse> saveFiles(
            @PathVariable Long projectId,
            @RequestBody ProjectFileSaveRequest request
    ) {
        ProjectFileSaveResponse response =
                projectFileService.saveFiles(projectId, request);

        return ResponseEntity.ok(response);
    }
}
