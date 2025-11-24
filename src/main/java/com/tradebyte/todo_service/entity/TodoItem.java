package com.tradebyte.todo_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "todo_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoItem {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoStatus status;

    @Column(nullable = false)
    private OffsetDateTime creationDatetime;

    @Column(nullable = false)
    private OffsetDateTime dueDatetime;

    private OffsetDateTime doneDatetime;
}