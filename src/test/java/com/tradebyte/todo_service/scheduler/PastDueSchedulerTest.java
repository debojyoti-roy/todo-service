package com.tradebyte.todo_service.scheduler;

import com.tradebyte.todo_service.service.TodoService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class PastDueSchedulerTest {

    @Test
    void schedulerShouldInvokeService() {
        TodoService service = mock(TodoService.class);
        PastDueScheduler scheduler = new PastDueScheduler(service);

        scheduler.markPastDue();

        verify(service, times(1)).markPastDueIfRequired();
    }
}
