package com.tradebyte.todo_service.repository;

import com.tradebyte.todo_service.entity.TodoItem;
import com.tradebyte.todo_service.entity.TodoStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class TodoRepositoryTest {

    @Autowired
    private TodoRepository repo;

    @Test
    void saveAndFind_shouldWork() {
        TodoItem item = new TodoItem();
        item.setDescription("Repo Test");
        item.setStatus(TodoStatus.NOT_DONE);
        item.setCreationDatetime(OffsetDateTime.now());
        item.setDueDatetime(OffsetDateTime.now().plusHours(1));

        TodoItem saved = repo.save(item);

        assertThat(repo.findById(saved.getId())).isPresent();
    }
}
