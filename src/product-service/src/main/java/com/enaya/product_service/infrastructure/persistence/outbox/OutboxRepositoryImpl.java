package com.enaya.product_service.infrastructure.persistence.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRepositoryImpl {
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public OutboxEvent save(OutboxEvent event) {
        try {
            return outboxEventRepository.save(event);
        } catch (Exception e) {
            log.error("Error saving outbox event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save outbox event", e);
        }
    }

    @Transactional(readOnly = true)
    public Optional<OutboxEvent> findById(UUID id) {
        return outboxEventRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<OutboxEvent> findByStatus(OutboxEventStatus status) {
        return outboxEventRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<OutboxEvent> findByStatus(OutboxEventStatus status, Pageable pageable) {
        return outboxEventRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public List<OutboxEvent> findFailedEventsForRetry(int maxRetries, Pageable pageable) {
        return outboxEventRepository.findFailedEventsForRetry(maxRetries, pageable).getContent();
    }

    @Transactional
    public void delete(OutboxEvent event) {
        try {
            outboxEventRepository.delete(event);
        } catch (Exception e) {
            log.error("Error deleting outbox event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete outbox event", e);
        }
    }

    @Transactional
    public void deleteAll(List<OutboxEvent> events) {
        try {
            outboxEventRepository.deleteAll(events);
        } catch (Exception e) {
            log.error("Error deleting outbox events: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete outbox events", e);
        }
    }

    @Transactional
    public int deletePublishedEventsOlderThan(Instant cutoffDate) {
        try {
            return outboxEventRepository.deletePublishedEventsOlderThan(cutoffDate);
        } catch (Exception e) {
            log.error("Error deleting old published events: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete old published events", e);
        }
    }

    @Transactional
    public int markAsPublished(UUID id, OutboxEventStatus newStatus, Instant publishedAt) {
        try {
            return outboxEventRepository.markAsPublished(id, newStatus, publishedAt);
        } catch (Exception e) {
            log.error("Error marking event as published: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to mark event as published", e);
        }
    }

    @Transactional
    public int markAsFailed(UUID id, OutboxEventStatus newStatus, String error) {
        try {
            return outboxEventRepository.markAsFailed(id, newStatus, error);
        } catch (Exception e) {
            log.error("Error marking event as failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to mark event as failed", e);
        }
    }

    @Transactional(readOnly = true)
    public long countByStatus(OutboxEventStatus status) {
        return outboxEventRepository.countByStatus(status);
    }
} 