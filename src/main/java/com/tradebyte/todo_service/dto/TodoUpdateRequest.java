package com.tradebyte.todo_service.dto;

import jakarta.validation.constraints.NotBlank;

public record TodoUpdateRequest(
        @NotBlank
        String description
) {}
