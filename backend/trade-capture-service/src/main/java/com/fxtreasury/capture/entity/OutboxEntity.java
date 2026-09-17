package com.fxtreasury.capture.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEntity {
    @Id
    private UUID id;
    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;
    @Column(name = "event_type", nullable = false)
    private String eventType;
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}