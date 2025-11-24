package com.tradebyte.todo_service.controller;

import com.tradebyte.todo_service.dto.TodoCreateRequest;
import com.tradebyte.todo_service.dto.TodoResponse;
import com.tradebyte.todo_service.dto.TodoUpdateRequest;
import com.tradebyte.todo_service.service.TodoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing Todos.
 * Provides endpoints for creating, updating, marking done/not done,
 * retrieving by ID, and listing todos.
 */
@RestController
@RequestMapping("/api/v1/todos")
public class TodoController {

    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }


    /**
     * Create a new Todo.
     *
     * @param req the Todo create request
     * @return the created Todo
     */
    @Operation(summary = "Create a new Todo", description = "Creates a new todo item.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Todo created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody TodoCreateRequest req) {
        logger.info("Creating new todo with description: {}", req.description());
        TodoResponse resp = service.create(req);
        logger.info("Created todo with id: {}", resp.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }


    /**
     * Update the description of an existing Todo.
     *
     * @param id  the Todo ID
     * @param req the update request
     * @return the updated Todo
     */
    @Operation(summary = "Update Todo description", description = "Updates the description of a todo by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todo updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Todo not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/{id}/description")
    public TodoResponse updateDescription(
            @Parameter(description = "ID of the todo to update") @PathVariable UUID id,
            @Valid @RequestBody TodoUpdateRequest req
    ) {
        logger.info("Updating description for todo with id: {}", id);
        TodoResponse updated = service.updateDescription(id, req);
        logger.info("Updated description for todo with id: {}", id);
        return updated;
    }


    /**
     * Mark a Todo as done.
     *
     * @param id the Todo ID
     * @return the updated Todo
     */
    @Operation(summary = "Mark Todo as done", description = "Marks a todo as done by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todo marked as done",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Todo not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/{id}/done")
    public TodoResponse markDone(@PathVariable UUID id) {
        logger.info("Marking todo as done with id: {}", id);
        TodoResponse resp = service.markDone(id);
        logger.info("Marked todo as done with id: {}", id);
        return resp;
    }


    /**
     * Mark a Todo as not done.
     *
     * @param id the Todo ID
     * @return the updated Todo
     */
    @Operation(summary = "Mark Todo as not done", description = "Marks a todo as not done by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todo marked as not done",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Todo not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/{id}/not-done")
    public TodoResponse markNotDone(@PathVariable UUID id) {
        logger.info("Marking todo as not done with id: {}", id);
        TodoResponse resp = service.markNotDone(id);
        logger.info("Marked todo as not done with id: {}", id);
        return resp;
    }


    /**
     * Get a Todo by ID.
     *
     * @param id the Todo ID
     * @return the requested Todo
     */
    @Operation(summary = "Get Todo by ID", description = "Retrieves a todo by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todo retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Todo not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{id}")
    public TodoResponse getById(@PathVariable UUID id) {
        logger.info("Fetching todo with id: {}", id);
        TodoResponse resp = service.getById(id);
        logger.info("Fetched todo with id: {}", id);
        return resp;
    }


    /**
     * List todos.
     *
     * @param all if true, returns all todos; otherwise, only todos not done
     * @return list of todos
     */
    @Operation(summary = "List Todos", description = "Lists all todos or only not done todos based on query parameter.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Todos listed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public List<TodoResponse> list(
            @Parameter(description = "If true, fetch all todos; otherwise only not done todos")
            @RequestParam(name = "all", required = false, defaultValue = "false")
            boolean all
    ) {
        logger.info("Listing todos with all={}", all);
        List<TodoResponse> todos = service.getNotDoneOrAll(all);
        logger.info("Retrieved {} todos", todos.size());
        return todos;
    }
}