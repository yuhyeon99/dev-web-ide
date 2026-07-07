package com.yuhyeon.devwebide.project.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectFileRenameRequest(
        @NotBlank(message = "파일/폴더 이름은 필수입니다.")
        String name
) {
}
