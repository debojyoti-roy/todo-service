package com.tradebyte.todo_service.exception;

import java.util.UUID;

public class NotFoundException extends RuntimeException {
    public NotFoundException(UUID id) { super("Item " + id + " not found"); }
}