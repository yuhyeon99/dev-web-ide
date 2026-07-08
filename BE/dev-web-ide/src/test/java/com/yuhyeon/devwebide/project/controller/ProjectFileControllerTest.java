package com.yuhyeon.devwebide.project.controller;

import com.yuhyeon.devwebide.project.domain.ProjectFileStatus;
import com.yuhyeon.devwebide.project.domain.ProjectFileType;
import com.yuhyeon.devwebide.project.domain.ProjectSaveBatchStatus;
import com.yuhyeon.devwebide.project.dto.*;
import com.yuhyeon.devwebide.project.service.ProjectFileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yuhyeon.devwebide.project.dto.ProjectFileSaveItemRequest;
import com.yuhyeon.devwebide.project.dto.ProjectFileSaveRequest;
import com.yuhyeon.devwebide.project.dto.ProjectFileSaveResponse;
import com.yuhyeon.devwebide.project.dto.SavedFileResponse;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(ProjectFileController.class)
class ProjectFileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectFileService projectFileService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("프로젝트 파일 트리를 조회한다")
    void getFileTree() throws Exception {
        // given
        Long projectId = 1L;

        LocalDateTime now = LocalDateTime.of(
                2026,
                7,
                1,
                10,
                0
        );

        ProjectFileTreeResponse app = new ProjectFileTreeResponse(
                3L,
                2L,
                "App.jsx",
                "/src/App.jsx",
                ProjectFileType.FILE,
                "text/javascript",
                120L,
                ProjectFileStatus.ACTIVE,
                now,
                now,
                List.of()
        );

        ProjectFileTreeResponse src = new ProjectFileTreeResponse(
                2L,
                1L,
                "src",
                "/src",
                ProjectFileType.DIRECTORY,
                null,
                0L,
                ProjectFileStatus.ACTIVE,
                now,
                now,
                List.of(app)
        );

        ProjectFileTreeResponse readme = new ProjectFileTreeResponse(
                4L,
                1L,
                "README.md",
                "/README.md",
                ProjectFileType.FILE,
                "text/markdown",
                40L,
                ProjectFileStatus.ACTIVE,
                now,
                now,
                List.of()
        );

        ProjectFileTreeResponse root = new ProjectFileTreeResponse(
                1L,
                null,
                "/",
                "/",
                ProjectFileType.DIRECTORY,
                null,
                0L,
                ProjectFileStatus.ACTIVE,
                now,
                now,
                List.of(src, readme)
        );

        given(projectFileService.getFileTree(projectId))
                .willReturn(List.of(root));

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/files/tree", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))

                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].parentFileId").doesNotExist())
                .andExpect(jsonPath("$[0].name").value("/"))
                .andExpect(jsonPath("$[0].path").value("/"))
                .andExpect(jsonPath("$[0].fileType").value("DIRECTORY"))
                .andExpect(jsonPath("$[0].mimeType").doesNotExist())
                .andExpect(jsonPath("$[0].sizeBytes").value(0L))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].children", hasSize(2)))

                .andExpect(jsonPath("$[0].children[0].id").value(2L))
                .andExpect(jsonPath("$[0].children[0].parentFileId").value(1L))
                .andExpect(jsonPath("$[0].children[0].name").value("src"))
                .andExpect(jsonPath("$[0].children[0].path").value("/src"))
                .andExpect(jsonPath("$[0].children[0].fileType").value("DIRECTORY"))
                .andExpect(jsonPath("$[0].children[0].sizeBytes").value(0L))
                .andExpect(jsonPath("$[0].children[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].children[0].children", hasSize(1)))

                .andExpect(jsonPath("$[0].children[0].children[0].id").value(3L))
                .andExpect(jsonPath("$[0].children[0].children[0].parentFileId").value(2L))
                .andExpect(jsonPath("$[0].children[0].children[0].name").value("App.jsx"))
                .andExpect(jsonPath("$[0].children[0].children[0].path").value("/src/App.jsx"))
                .andExpect(jsonPath("$[0].children[0].children[0].fileType").value("FILE"))
                .andExpect(jsonPath("$[0].children[0].children[0].mimeType").value("text/javascript"))
                .andExpect(jsonPath("$[0].children[0].children[0].sizeBytes").value(120L))
                .andExpect(jsonPath("$[0].children[0].children[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].children[0].children[0].children", hasSize(0)))

                .andExpect(jsonPath("$[0].children[1].id").value(4L))
                .andExpect(jsonPath("$[0].children[1].parentFileId").value(1L))
                .andExpect(jsonPath("$[0].children[1].name").value("README.md"))
                .andExpect(jsonPath("$[0].children[1].path").value("/README.md"))
                .andExpect(jsonPath("$[0].children[1].fileType").value("FILE"))
                .andExpect(jsonPath("$[0].children[1].mimeType").value("text/markdown"))
                .andExpect(jsonPath("$[0].children[1].sizeBytes").value(40L))
                .andExpect(jsonPath("$[0].children[1].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].children[1].children", hasSize(0)));

        then(projectFileService).should()
                .getFileTree(projectId);
    }

    @Test
    @DisplayName("프로젝트 파일 트리가 비어 있으면 빈 배열을 반환한다")
    void getFileTree_empty() throws Exception {
        // given
        Long projectId = 1L;

        given(projectFileService.getFileTree(projectId))
                .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/projects/{projectId}/files/tree", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        then(projectFileService).should()
                .getFileTree(projectId);
    }

    @Test
    @DisplayName("프로젝트 파일 내용을 조회한다")
    void getFileContent() throws Exception {
        Long projectId = 1L;
        Long fileId = 10L;
        LocalDateTime updatedAt = LocalDateTime.of(2026, 7, 7, 10, 0);

        ProjectFileContentResponse response = new ProjectFileContentResponse(
                fileId,
                "App.jsx",
                "/src/App.jsx",
                "text/javascript",
                120L,
                "console.log('hello');",
                updatedAt
        );

        given(projectFileService.getFileContent(projectId, fileId))
                .willReturn(response);

        mockMvc.perform(get(
                        "/api/projects/{projectId}/files/{fileId}/content",
                        projectId,
                        fileId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectFileId").value(fileId))
                .andExpect(jsonPath("$.name").value("App.jsx"))
                .andExpect(jsonPath("$.path").value("/src/App.jsx"))
                .andExpect(jsonPath("$.mimeType").value("text/javascript"))
                .andExpect(jsonPath("$.sizeBytes").value(120L))
                .andExpect(jsonPath("$.content").value("console.log('hello');"))
                .andExpect(jsonPath("$.updatedAt").exists());

        then(projectFileService).should()
                .getFileContent(projectId, fileId);
    }

    @Test
    @DisplayName("프로젝트 파일을 생성한다")
    void createFile() throws Exception {
        Long projectId = 1L;
        LocalDateTime now = LocalDateTime.of(2026, 7, 7, 10, 0);

        ProjectFileCreateRequest request = new ProjectFileCreateRequest(
                1L,
                "App.jsx",
                ProjectFileType.FILE
        );

        ProjectFileCreateResponse response = new ProjectFileCreateResponse(
                10L,
                1L,
                "App.jsx",
                "/src/App.jsx",
                ProjectFileType.FILE,
                "text/javascript",
                0L,
                ProjectFileStatus.ACTIVE,
                now,
                now
        );

        given(projectFileService.createFile(eq(projectId), refEq(request)))
                .willReturn(response);

        mockMvc.perform(post("/api/projects/{projectId}/files", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectFileId").value(10L))
                .andExpect(jsonPath("$.parentFileId").value(1L))
                .andExpect(jsonPath("$.name").value("App.jsx"))
                .andExpect(jsonPath("$.path").value("/src/App.jsx"))
                .andExpect(jsonPath("$.fileType").value("FILE"))
                .andExpect(jsonPath("$.mimeType").value("text/javascript"))
                .andExpect(jsonPath("$.sizeBytes").value(0L))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        then(projectFileService).should()
                .createFile(eq(projectId), refEq(request));
    }

    @Test
    @DisplayName("프로젝트 디렉터리를 생성한다")
    void createDirectory() throws Exception {
        Long projectId = 1L;
        LocalDateTime now = LocalDateTime.of(2026, 7, 7, 10, 0);

        ProjectFileCreateRequest request = new ProjectFileCreateRequest(
                1L,
                "components",
                ProjectFileType.DIRECTORY
        );

        ProjectFileCreateResponse response = new ProjectFileCreateResponse(
                11L,
                1L,
                "components",
                "/src/components",
                ProjectFileType.DIRECTORY,
                null,
                0L,
                ProjectFileStatus.ACTIVE,
                now,
                now
        );

        given(projectFileService.createFile(eq(projectId), refEq(request)))
                .willReturn(response);

        mockMvc.perform(post("/api/projects/{projectId}/files", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectFileId").value(11L))
                .andExpect(jsonPath("$.parentFileId").value(1L))
                .andExpect(jsonPath("$.name").value("components"))
                .andExpect(jsonPath("$.path").value("/src/components"))
                .andExpect(jsonPath("$.fileType").value("DIRECTORY"))
                .andExpect(jsonPath("$.mimeType").doesNotExist())
                .andExpect(jsonPath("$.sizeBytes").value(0L))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        then(projectFileService).should()
                .createFile(eq(projectId), refEq(request));
    }

    @Test
    @DisplayName("?꾨줈?앺듃 ?뚯씪 ?대쫫??蹂寃쏀븳??")
    void renameFile() throws Exception {
        Long projectId = 1L;
        Long fileId = 10L;
        LocalDateTime now = LocalDateTime.of(2026, 7, 7, 10, 0);

        ProjectFileRenameRequest request = new ProjectFileRenameRequest("App.tsx");

        ProjectFileCreateResponse response = new ProjectFileCreateResponse(
                fileId,
                1L,
                "App.tsx",
                "/src/App.tsx",
                ProjectFileType.FILE,
                "text/typescript",
                120L,
                ProjectFileStatus.ACTIVE,
                now,
                now
        );

        given(projectFileService.renameFile(eq(projectId), eq(fileId), refEq(request)))
                .willReturn(response);

        mockMvc.perform(patch(
                        "/api/projects/{projectId}/files/{fileId}/rename",
                        projectId,
                        fileId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectFileId").value(fileId))
                .andExpect(jsonPath("$.parentFileId").value(1L))
                .andExpect(jsonPath("$.name").value("App.tsx"))
                .andExpect(jsonPath("$.path").value("/src/App.tsx"))
                .andExpect(jsonPath("$.fileType").value("FILE"))
                .andExpect(jsonPath("$.mimeType").value("text/typescript"))
                .andExpect(jsonPath("$.sizeBytes").value(120L))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        then(projectFileService).should()
                .renameFile(eq(projectId), eq(fileId), refEq(request));
    }

    @Test
    @DisplayName("프로젝트 파일을 삭제한다")
    void deleteFile() throws Exception {
        Long projectId = 1L;
        Long fileId = 10L;
        LocalDateTime updatedAt = LocalDateTime.of(2026, 7, 8, 10, 0);

        ProjectFileDeleteResponse response = new ProjectFileDeleteResponse(
                fileId,
                ProjectFileType.FILE,
                "/src/App.jsx",
                ProjectFileStatus.DELETED,
                true,
                updatedAt
        );

        given(projectFileService.deleteFile(projectId, fileId))
                .willReturn(response);

        mockMvc.perform(delete(
                        "/api/projects/{projectId}/files/{fileId}",
                        projectId,
                        fileId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectFileId").value(fileId))
                .andExpect(jsonPath("$.fileType").value("FILE"))
                .andExpect(jsonPath("$.path").value("/src/App.jsx"))
                .andExpect(jsonPath("$.status").value("DELETED"))
                .andExpect(jsonPath("$.deleted").value(true))
                .andExpect(jsonPath("$.updatedAt").exists());

        then(projectFileService).should()
                .deleteFile(projectId, fileId);
    }

    @Test
    @DisplayName("프로젝트 파일을 저장한다")
    void saveFiles() throws Exception {
        ProjectFileSaveRequest request = new ProjectFileSaveRequest(
                1L,
                null,
                List.of(
                        new ProjectFileSaveItemRequest(
                                10L,
                                "console.log('hello');"
                        )
                )
        );

        ProjectFileSaveResponse response = new ProjectFileSaveResponse(
                1L,
                100L,
                ProjectSaveBatchStatus.SUCCESS,
                1,
                List.of(
                        new SavedFileResponse(
                                10L,
                                "/src/index.js",
                                1,
                                21L,
                                "hash-abc"
                        )
                )
        );

        given(projectFileService.saveFiles(eq(1L), refEq(request)))
                .willReturn(response);

        mockMvc.perform(post("/api/projects/{projectId}/save", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1L))
                .andExpect(jsonPath("$.saveBatchId").value(100L))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.savedFileCount").value(1))
                .andExpect(jsonPath("$.files", hasSize(1)))
                .andExpect(jsonPath("$.files[0].projectFileId").value(10L))
                .andExpect(jsonPath("$.files[0].path").value("/src/index.js"))
                .andExpect(jsonPath("$.files[0].versionNo").value(1))
                .andExpect(jsonPath("$.files[0].sizeBytes").value(21L))
                .andExpect(jsonPath("$.files[0].contentHash").value("hash-abc"));

        then(projectFileService).should()
                .saveFiles(eq(1L), refEq(request));
    }
}
