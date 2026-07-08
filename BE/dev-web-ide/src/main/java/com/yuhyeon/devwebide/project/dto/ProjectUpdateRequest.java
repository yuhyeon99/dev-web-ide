package com.yuhyeon.devwebide.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectUpdateRequest(
        @NotBlank(message = "프로젝트 이름은 필수입니다.")
        @Size(max = 200, message = "프로젝트 이름은 200자 이하여야 합니다.")
        String name,

        @Size(max = 500, message = "프로젝트 설명은 500자 이하여야 합니다.")
        String description
) {
}
