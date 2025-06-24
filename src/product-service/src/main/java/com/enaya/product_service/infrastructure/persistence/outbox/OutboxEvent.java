package com.enaya.product_service.infrastructure.persistence.outbox;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String eventType;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @NotBlank
    @Column(nullable = false)
    private String topic;

    @NotBlank
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @NotBlank
    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status;

    @Column
    private String lastError;

    @Column
    private Integer retryCount;

    @Column
    private Instant publishedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    private LocalDateTime processedAt;

    public OutboxEvent(String eventType, String payload, String topic, String aggregateId, String aggregateType) {
        this.eventType = eventType;
        this.payload = payload;
        this.topic = topic;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.status = OutboxEventStatus.PENDING;
        this.retryCount = 0;
    }

    public void markAsPublished() {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.lastError = null;
    }

    public void markAsFailed(String error) {
        this.status = OutboxEventStatus.FAILED;
        this.lastError = error;
        this.retryCount++;
    }

    public boolean canRetry(int maxRetries) {
        return this.retryCount < maxRetries;
    }

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = OutboxEventStatus.PENDING;
        }
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}