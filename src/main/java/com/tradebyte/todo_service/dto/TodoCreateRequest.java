package com.tradebyte.todo_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record TodoCreateRequest(
        @NotBlank
        String description,

        @NotNull
        OffsetDateTime dueDatetime
) {}