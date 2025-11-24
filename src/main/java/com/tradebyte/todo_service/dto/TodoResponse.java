package com.tradebyte.todo_service.dto;

import com.tradebyte.todo_service.entity.TodoStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TodoResponse(
        UUID id,
        String description,
        TodoStatus status,
        OffsetDateTime creationDatetime,
        OffsetDateTime dueDatetime,
        OffsetDateTime doneDatetime
) {}
