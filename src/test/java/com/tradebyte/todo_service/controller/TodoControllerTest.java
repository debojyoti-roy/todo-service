package com.tradebyte.todo_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tradebyte.todo_service.dto.TodoCreateRequest;
import com.tradebyte.todo_service.dto.TodoResponse;
import com.tradebyte.todo_service.dto.TodoUpdateRequest;
import com.tradebyte.todo_service.entity.TodoStatus;
import com.tradebyte.todo_service.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link TodoController}. Uses MockMvc and Mockito to verify controller behavior,
 * request/response correctness, and proper HTTP status handling.
 */
class TodoControllerTest {

    private MockMvc mvc;
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoController todoController;

    /**
     * Initializes Mockito mocks, registers JavaTimeModule for OffsetDateTime serialization,
     * and sets up a standalone MockMvc instance for controller testing.
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper.registerModule(new JavaTimeModule());
        mvc = MockMvcBuilders.standaloneSetup(todoController).build();
    }

    /**
     * Tests POST /api/v1/todos to ensure that a todo is created successfully
     * and the controller returns HTTP 201 (Created) with the expected response body.
     */
    @Test
    void create_shouldReturn201() throws Exception {
        UUID id = UUID.randomUUID();
        OffsetDateTime dueTime = OffsetDateTime.now().plusDays(1).withNano(0);
        TodoCreateRequest req = new TodoCreateRequest("New Task", dueTime);

        OffsetDateTime createdTime = OffsetDateTime.now().withNano(0);
        TodoResponse mockResp = new TodoResponse(
                id, req.description(), TodoStatus.NOT_DONE, createdTime, req.dueDatetime(), null
        );

        Mockito.when(todoService.create(any(TodoCreateRequest.class))).thenReturn(mockResp);

        mvc.perform(post("/api/v1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.description").value("New Task"))
                .andExpect(jsonPath("$.status").value("NOT_DONE"));
    }

    /**
     * Tests POST /api/v1/todos/{id}/done to ensure a todo can be marked as DONE
     * and the controller returns HTTP 200 with updated status.
     */
    @Test
    void markDone_shouldReturn200AndDONEStatus() throws Exception {
        UUID id = UUID.randomUUID();
        OffsetDateTime creationTime = OffsetDateTime.now().withNano(0);

        TodoResponse doneResp = new TodoResponse(
                id,
                "Task to be done",
                TodoStatus.DONE,
                creationTime,
                creationTime.plusDays(1),
                OffsetDateTime.now().withNano(0)
        );
        Mockito.when(todoService.markDone(eq(id))).thenReturn(doneResp);

        mvc.perform(post("/api/v1/todos/" + id + "/done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.doneDatetime").exists());

        Mockito.verify(todoService).markDone(id);
    }

    /**
     * Tests POST /api/v1/todos/{id}/not-done to verify that a todo can be reverted
     * back to NOT_DONE status and returns HTTP 200.
     */
    @Test
    void markNotDone_shouldReturn200AndNOT_DONEStatus() throws Exception {
        UUID id = UUID.randomUUID();
        OffsetDateTime creationTime = OffsetDateTime.now().withNano(0);

        TodoResponse notDoneResp = new TodoResponse(
                id,
                "Task to be not done",
                TodoStatus.NOT_DONE,
                creationTime,
                creationTime.plusDays(1),
                null
        );
        Mockito.when(todoService.markNotDone(eq(id))).thenReturn(notDoneResp);

        mvc.perform(post("/api/v1/todos/" + id + "/not-done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_DONE"))
                .andExpect(jsonPath("$.doneDatetime").doesNotExist());

        Mockito.verify(todoService).markNotDone(id);
    }

    /**
     * Tests PATCH /api/v1/todos/{id}/description to verify that the description is updated
     * successfully and returns the updated value.
     */
    @Test
    void updateDescription_shouldReturn200AndNewDescription() throws Exception {
        UUID id = UUID.randomUUID();
        String newDescription = "Updated Task Description";
        TodoUpdateRequest req = new TodoUpdateRequest(newDescription);
        OffsetDateTime creationTime = OffsetDateTime.now().withNano(0);

        TodoResponse updatedResp = new TodoResponse(
                id,
                newDescription,
                TodoStatus.NOT_DONE,
                creationTime,
                creationTime.plusDays(1),
                null
        );

        Mockito.when(todoService.updateDescription(eq(id), any(TodoUpdateRequest.class))).thenReturn(updatedResp);

        mvc.perform(patch("/api/v1/todos/" + id + "/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(newDescription));
    }

    /**
     * Tests GET /api/v1/todos/{id} to ensure the correct todo is returned
     * with status 200 and expected fields.
     */
    @Test
    void getById_shouldReturn200AndTodo() throws Exception {
        UUID id = UUID.randomUUID();
        TodoResponse mockResp = new TodoResponse(
                id,
                "Fetch Test Task",
                TodoStatus.NOT_DONE,
                OffsetDateTime.now().withNano(0),
                OffsetDateTime.now().plusDays(1).withNano(0),
                null
        );

        Mockito.when(todoService.getById(eq(id))).thenReturn(mockResp);

        mvc.perform(get("/api/v1/todos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.description").value("Fetch Test Task"));
    }

    /**
     * Tests GET /api/v1/todos with no parameters, verifying default behavior
     * returns only NOT_DONE todos and HTTP 200.
     */
    @Test
    void list_withNoParams_shouldReturn200AndNotDoneTodos() throws Exception {
        TodoResponse todo1 = new TodoResponse(
                UUID.randomUUID(), "Task 1", TodoStatus.NOT_DONE,
                OffsetDateTime.now(), OffsetDateTime.now().plusDays(1), null
        );
        List<TodoResponse> mockList = Collections.singletonList(todo1);

        Mockito.when(todoService.getNotDoneOrAll(eq(false))).thenReturn(mockList);

        mvc.perform(get("/api/v1/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Task 1"));
    }

    /**
     * Tests GET /api/v1/todos?all=true to verify that all todos (including DONE)
     * are returned with HTTP 200.
     */
    @Test
    void list_withAllTrue_shouldReturn200AndAllTodos() throws Exception {
        TodoResponse todo1 = new TodoResponse(
                UUID.randomUUID(), "Task 1", TodoStatus.NOT_DONE,
                OffsetDateTime.now(), OffsetDateTime.now().plusDays(1), null
        );
        TodoResponse todo2 = new TodoResponse(
                UUID.randomUUID(), "Task 2", TodoStatus.DONE,
                OffsetDateTime.now(), OffsetDateTime.now().plusDays(1), OffsetDateTime.now()
        );
        List<TodoResponse> mockList = List.of(todo1, todo2);

        Mockito.when(todoService.getNotDoneOrAll(eq(true))).thenReturn(mockList);

        mvc.perform(get("/api/v1/todos?all=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}