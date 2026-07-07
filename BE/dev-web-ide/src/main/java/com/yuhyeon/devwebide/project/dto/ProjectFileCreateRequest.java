package com.yuhyeon.devwebide.project.dto;

import com.yuhyeon.devwebide.project.domain.ProjectFileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectFileCreateRequest(
        Long parentFileId,

        @NotBlank(message = "파일/폴더 이름은 필수입니다.")
        String name,

        @NotNull(message = "파일 타입은 필수입니다.")
        ProjectFileType fileType
) {
}
