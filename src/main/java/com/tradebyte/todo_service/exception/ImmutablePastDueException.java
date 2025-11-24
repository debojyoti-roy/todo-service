package com.tradebyte.todo_service.exception;

import java.util.UUID;

public class ImmutablePastDueException extends RuntimeException {
    public ImmutablePastDueException(UUID id) {
        super("Item " + id + " is past due and cannot be modified.");
    }
}
