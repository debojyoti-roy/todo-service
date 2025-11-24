package com.tradebyte.todo_service.scheduler;

import com.tradebyte.todo_service.service.TodoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PastDueScheduler {

    private final TodoService service;
    private final Logger logger = LoggerFactory.getLogger(PastDueScheduler.class);

    public PastDueScheduler(TodoService service) {
        this.service = service;
    }

    // runs every minute; adjust interval as needed
    @Scheduled(fixedDelayString = "PT1M")
    public void markPastDue() {
        int updated = service.markPastDueIfRequired();
        if (updated > 0) {
            logger.info("Marked {} todo(s) as PAST_DUE", updated);
        }
    }
}
