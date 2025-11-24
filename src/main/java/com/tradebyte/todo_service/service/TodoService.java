package com.tradebyte.todo_service.service;

import com.tradebyte.todo_service.dto.TodoCreateRequest;
import com.tradebyte.todo_service.dto.TodoResponse;
import com.tradebyte.todo_service.dto.TodoUpdateRequest;
import com.tradebyte.todo_service.entity.TodoItem;
import com.tradebyte.todo_service.entity.TodoStatus;
import com.tradebyte.todo_service.exception.ImmutablePastDueException;
import com.tradebyte.todo_service.exception.NotFoundException;
import com.tradebyte.todo_service.repository.TodoRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing Todo items.
 * Provides operations for creating, updating, marking done/not done,
 * retrieving, listing, and marking past due Todos.
 */
@Service
public class TodoService {

    private static final Logger logger = LoggerFactory.getLogger(TodoService.class);

    private final TodoRepository repo;

    public TodoService(TodoRepository repo) {
        this.repo = repo;
    }


    /**
     * Creates a new Todo item.
     *
     * @param req the Todo creation request
     * @return the created TodoResponse
     */
    @Transactional
    public TodoResponse create(TodoCreateRequest req) {
        logger.info("Creating new todo with description: {}", req.description());

        TodoItem item = new TodoItem();
        item.setDescription(req.description());
        item.setCreationDatetime(OffsetDateTime.now());
        item.setDueDatetime(req.dueDatetime());
        item.setStatus(TodoStatus.NOT_DONE);

        item = repo.save(item);

        logger.info("Created todo with id: {}", item.getId());
        return map(item);
    }


    /**
     * Updates the description of an existing Todo.
     *
     * @param id  the Todo ID
     * @param req the update request
     * @return the updated TodoResponse
     */
    @CachePut(value = "todoById", key = "#id")
    @CacheEvict(value = "todoList", allEntries = true)
    @CircuitBreaker(name = "todoServiceCB", fallbackMethod = "fallbackUpdate")
    @RateLimiter(name = "todoRateLimiter", fallbackMethod = "fallbackRateLimited")
    @Transactional
    public TodoResponse updateDescription(UUID id, TodoUpdateRequest req) {
        logger.info("Updating description for todo with id: {}", id);

        TodoItem item = repo.findById(id).orElseThrow(() -> {
            logger.warn("Todo not found with id: {}", id);
            return new NotFoundException(id);
        });
        ensureMutable(item);

        item.setDescription(req.description());
        repo.save(item);

        logger.info("Updated description for todo with id: {}", id);
        return map(item);
    }


    /**
     * Marks a Todo as done.
     *
     * @param id the Todo ID
     * @return the updated TodoResponse
     */
    @CachePut(value = "todoById", key = "#id")
    @CacheEvict(value = "todoList", allEntries = true)
    @Transactional
    public TodoResponse markDone(UUID id) {
        logger.info("Marking todo as done with id: {}", id);

        TodoItem item = repo.findById(id).orElseThrow(() -> {
            logger.warn("Todo not found with id: {}", id);
            return new NotFoundException(id);
        });
        ensureMutable(item);

        item.setStatus(TodoStatus.DONE);
        item.setDoneDatetime(OffsetDateTime.now());
        repo.save(item);

        logger.info("Marked todo as done with id: {}", id);
        return map(item);
    }


    /**
     * Marks a Todo as not done.
     *
     * @param id the Todo ID
     * @return the updated TodoResponse
     */
    @CachePut(value = "todoById", key = "#id")
    @CacheEvict(value = "todoList", allEntries = true)
    @Transactional
    public TodoResponse markNotDone(UUID id) {
        logger.info("Marking todo as not done with id: {}", id);

        TodoItem item = repo.findById(id).orElseThrow(() -> {
            logger.warn("Todo not found with id: {}", id);
            return new NotFoundException(id);
        });
        ensureMutable(item);

        item.setStatus(TodoStatus.NOT_DONE);
        item.setDoneDatetime(null);
        repo.save(item);

        logger.info("Marked todo as not done with id: {}", id);
        return map(item);
    }


    /**
     * Retrieves a Todo by ID.
     *
     * @param id the Todo ID
     * @return the TodoResponse
     */
    @Cacheable(value = "todoById", key = "#id")
    @CircuitBreaker(name = "todoServiceCB", fallbackMethod = "fallbackGetById")
    @RateLimiter(name = "todoRateLimiter", fallbackMethod = "fallbackRateLimited")
    @Transactional(readOnly = true)
    public TodoResponse getById(UUID id) {
        logger.info("Fetching todo with id: {}", id);

        TodoItem item = repo.findById(id).orElseThrow(() -> {
            logger.warn("Todo not found with id: {}", id);
            return new NotFoundException(id);
        });

        return map(item);
    }


    /**
     * Lists all Todos or only Todos that are not done.
     *
     * @param all if true, fetch all todos; otherwise only not done todos
     * @return list of TodoResponse
     */
    @Cacheable(value = "todoList", key = "#all")
    @CircuitBreaker(name = "todoServiceCB", fallbackMethod = "fallbackList")
    @RateLimiter(name = "todoRateLimiter", fallbackMethod = "fallbackRateLimitedList")
    @Transactional(readOnly = true)
    public List<TodoResponse> getNotDoneOrAll(boolean all) {
        logger.info("Listing todos with all={}", all);

        List<TodoResponse> todos;
        if (all) {
            todos = repo.findAll().stream().map(this::map).collect(Collectors.toList());
        } else {
            todos = repo.findByStatus(TodoStatus.NOT_DONE)
                    .stream().map(this::map).collect(Collectors.toList());
        }

        logger.info("Retrieved {} todos", todos.size());
        return todos;
    }


    /**
     * SCHEDULER USES THIS
     * Marks Todos as past due if the due date has passed.
     *
     * @return the number of Todos marked as past due
     */
    @CacheEvict(value = {"todoById", "todoList"}, allEntries = true)
    @Transactional
    public int markPastDueIfRequired() {
        OffsetDateTime now = OffsetDateTime.now();

        List<TodoItem> toMark = repo.findByStatusAndDueDatetimeBefore(TodoStatus.NOT_DONE, now);

        toMark.forEach(item -> item.setStatus(TodoStatus.PAST_DUE));

        repo.saveAll(toMark);

        logger.info("Marked {} todos as past due", toMark.size());
        return toMark.size();
    }


    private void ensureMutable(TodoItem item) {
        if (item.getStatus() == TodoStatus.PAST_DUE) {
            logger.warn("Attempted to modify immutable past due todo with id: {}", item.getId());
            throw new ImmutablePastDueException(item.getId());
        }
    }

    private TodoResponse map(TodoItem i) {
        return new TodoResponse(
                i.getId(),
                i.getDescription(),
                i.getStatus(),
                i.getCreationDatetime(),
                i.getDueDatetime(),
                i.getDoneDatetime()
        );
    }

    public TodoResponse fallbackGetById(UUID id, Throwable ex) {
        logger.error("Fallback getById triggered for id {}: {}", id, ex.getMessage());
        return new TodoResponse(id, "Service unavailable", TodoStatus.PAST_DUE, null, null, null);
    }

    public List<TodoResponse> fallbackList(boolean fetchAll, Throwable ex) {
        logger.error("Fallback list triggered: {}", ex.getMessage());
        return List.of();
    }

    public TodoResponse fallbackUpdate(UUID id, TodoUpdateRequest req, Throwable ex) {
        logger.error("Fallback update triggered for id {}: {}", id, ex.getMessage());
        return new TodoResponse(id, "Update failed", null, null, null, null);
    }

    public TodoResponse fallbackRateLimited(UUID id, Throwable ex) {
        logger.warn("Rate limit exceeded for getById id {}: {}", id, ex.getMessage());
        throw new RuntimeException("Rate limit exceeded. Try again later.");
    }

    public List<TodoResponse> fallbackRateLimitedList(boolean fetchAll, Throwable ex) {
        logger.warn("Rate limit exceeded for list: {}", ex.getMessage());
        throw new RuntimeException("Rate limit exceeded. Try again later.");
    }
}