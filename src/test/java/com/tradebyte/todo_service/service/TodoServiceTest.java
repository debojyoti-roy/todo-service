package com.tradebyte.todo_service.service;

import com.tradebyte.todo_service.dto.TodoCreateRequest;
import com.tradebyte.todo_service.dto.TodoUpdateRequest;
import com.tradebyte.todo_service.entity.TodoItem;
import com.tradebyte.todo_service.entity.TodoStatus;
import com.tradebyte.todo_service.exception.ImmutablePastDueException;
import com.tradebyte.todo_service.exception.NotFoundException;
import com.tradebyte.todo_service.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TodoServiceTest {

    private TodoRepository repo;
    private TodoService service;

    @BeforeEach
    void setup() {
        repo = mock(TodoRepository.class);
        service = new TodoService(repo);
    }

    @Test
    void create_shouldSaveWithNotDoneStatus() {
        TodoCreateRequest req = new TodoCreateRequest(
                "task",
                OffsetDateTime.now().plusDays(1)
        );

        TodoItem saved = new TodoItem();
        saved.setId(UUID.randomUUID());
        saved.setDescription("task");
        saved.setStatus(TodoStatus.NOT_DONE);
        saved.setCreationDatetime(OffsetDateTime.now());
        saved.setDueDatetime(req.dueDatetime());

        when(repo.save(any(TodoItem.class))).thenReturn(saved);

        var result = service.create(req);

        assertThat(result.status()).isEqualTo(TodoStatus.NOT_DONE);
        verify(repo).save(any());
    }

    @Test
    void updateDescription_shouldThrowWhenPastDue() {
        UUID id = UUID.randomUUID();
        TodoItem item = new TodoItem();
        item.setId(id);
        item.setStatus(TodoStatus.PAST_DUE);

        when(repo.findById(id)).thenReturn(Optional.of(item));

        TodoUpdateRequest req = new TodoUpdateRequest("updated");

        assertThatThrownBy(() -> service.updateDescription(id, req))
                .isInstanceOf(ImmutablePastDueException.class);
    }

    @Test
    void markDone_shouldSetDoneDatetime() {
        UUID id = UUID.randomUUID();
        TodoItem item = new TodoItem();
        item.setId(id);
        item.setStatus(TodoStatus.NOT_DONE);

        when(repo.findById(id)).thenReturn(Optional.of(item));
        when(repo.save(item)).thenReturn(item);

        var resp = service.markDone(id);

        assertThat(resp.status()).isEqualTo(TodoStatus.DONE);
        assertThat(resp.doneDatetime()).isNotNull();
    }

    @Test
    void getById_notFoundShouldThrow() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void markPastDueIfRequired_shouldUpdatePastDueItems() {
        TodoItem notDone = new TodoItem();
        notDone.setId(UUID.randomUUID());
        notDone.setStatus(TodoStatus.NOT_DONE);
        notDone.setDueDatetime(OffsetDateTime.now().minusHours(1));

        when(repo.findByStatusAndDueDatetimeBefore(eq(TodoStatus.NOT_DONE), any()))
                .thenReturn(List.of(notDone));

        int count = service.markPastDueIfRequired();

        assertThat(count).isEqualTo(1);
        assertThat(notDone.getStatus()).isEqualTo(TodoStatus.PAST_DUE);
    }
}