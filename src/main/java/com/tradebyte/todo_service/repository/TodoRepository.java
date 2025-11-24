package com.tradebyte.todo_service.repository;

import com.tradebyte.todo_service.entity.TodoItem;
import com.tradebyte.todo_service.entity.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TodoRepository extends JpaRepository<TodoItem, UUID> {
    List<TodoItem> findByStatus(TodoStatus status);
    List<TodoItem> findByStatusAndDueDatetimeBefore(TodoStatus status, OffsetDateTime before);
}
