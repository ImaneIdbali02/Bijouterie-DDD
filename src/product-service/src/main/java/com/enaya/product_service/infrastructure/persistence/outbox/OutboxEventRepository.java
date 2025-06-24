package com.enaya.product_service.infrastructure.persistence.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OutboxEvent> findByStatus(OutboxEventStatus status);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Page<OutboxEvent> findByStatus(OutboxEventStatus status, Pageable pageable);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OutboxEvent> findByStatusAndRetryCountLessThan(OutboxEventStatus status, int maxRetries);
    
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'PUBLISHED' AND e.publishedAt < :cutoffDate")
    List<OutboxEvent> findPublishedEventsOlderThan(@Param("cutoffDate") Instant cutoffDate);
    
    @Query("SELECT e FROM OutboxEvent e WHERE e.aggregateId = :aggregateId AND e.aggregateType = :aggregateType")
    List<OutboxEvent> findByAggregateIdAndAggregateType(
        @Param("aggregateId") String aggregateId,
        @Param("aggregateType") String aggregateType
    );
    
    @Query("SELECT COUNT(e) FROM OutboxEvent e WHERE e.status = :status")
    long countByStatus(@Param("status") OutboxEventStatus status);
    
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'FAILED' AND e.retryCount < :maxRetries")
    Page<OutboxEvent> findFailedEventsForRetry(
        @Param("maxRetries") int maxRetries,
        Pageable pageable
    );
    
    @Modifying
    @Query("DELETE FROM OutboxEvent e WHERE e.status = 'PUBLISHED' AND e.publishedAt < :cutoffDate")
    int deletePublishedEventsOlderThan(@Param("cutoffDate") Instant cutoffDate);
    
    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = :newStatus, e.publishedAt = :publishedAt, e.lastError = NULL WHERE e.id = :id")
    int markAsPublished(
        @Param("id") UUID id,
        @Param("newStatus") OutboxEventStatus newStatus,
        @Param("publishedAt") Instant publishedAt
    );
    
    @Modifying
    @Query("UPDATE OutboxEvent e SET e.status = :newStatus, e.lastError = :error, e.retryCount = e.retryCount + 1 WHERE e.id = :id")
    int markAsFailed(
        @Param("id") UUID id,
        @Param("newStatus") OutboxEventStatus newStatus,
        @Param("error") String error
    );

    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'PENDING' ORDER BY e.createdAt ASC")
    List<OutboxEvent> findPendingEvents();

    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'FAILED' ORDER BY e.createdAt ASC")
    List<OutboxEvent> findFailedEvents();
}
