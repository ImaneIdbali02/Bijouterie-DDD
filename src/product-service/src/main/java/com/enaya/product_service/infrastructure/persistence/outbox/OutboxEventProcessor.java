package com.enaya.product_service.infrastructure.persistence.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {
    private final OutboxRepositoryImpl outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${outbox.batch-size:100}")
    private int batchSize;

    @Value("${outbox.max-retries:3}")
    private int maxRetries;

    @Value("${outbox.processor.fixed-delay:5000}")
    private String fixedDelay;

    @Value("${outbox.processor.retry-delay:300000}")
    private String retryDelay;

    @Transactional
    public void processEvent(OutboxEvent event) {
        if (!event.canRetry(maxRetries)) {
            log.warn("Event {} has exceeded maximum retry attempts", event.getId());
            return;
        }

        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                event.getTopic(),
                event.getPayload()
            );

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    handleSuccessfulPublish(event);
                } else {
                    handleFailedPublish(event, ex);
                }
            });
        } catch (Exception e) {
            handleFailedPublish(event, e);
        }
    }

    private void handleSuccessfulPublish(OutboxEvent event) {
        try {
            outboxRepository.markAsPublished(
                event.getId(),
                OutboxEventStatus.PUBLISHED,
                Instant.now()
            );
            log.info("Successfully published event {} to topic {}", event.getId(), event.getTopic());
        } catch (Exception e) {
            log.error("Error updating event status after successful publish: {}", e.getMessage(), e);
        }
    }

    private void handleFailedPublish(OutboxEvent event, Throwable ex) {
        try {
            outboxRepository.markAsFailed(
                event.getId(),
                OutboxEventStatus.FAILED,
                ex.getMessage()
            );
            log.error("Failed to publish event {} to topic {}: {}", 
                    event.getId(), event.getTopic(), ex.getMessage(), ex);
        } catch (Exception e) {
            log.error("Error updating event status after failed publish: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void processEventsInBatch(List<OutboxEvent> events) {
        for (OutboxEvent event : events) {
            try {
                processEvent(event);
            } catch (Exception e) {
                log.error("Error processing event in batch: {}", e.getMessage(), e);
            }
        }
    }

    @Scheduled(fixedDelayString = "${outbox.processor.fixed-delay:5000}")
    @Transactional
    public void processPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatus(OutboxEventStatus.PENDING);
        processEventsInBatch(pendingEvents);
    }

    @Scheduled(fixedDelayString = "${outbox.processor.retry-delay:300000}")
    @Transactional
    public void retryFailedEvents() {
        List<OutboxEvent> failedEvents = outboxRepository.findFailedEventsForRetry(
            maxRetries,
            org.springframework.data.domain.PageRequest.of(0, batchSize)
        );
        processEventsInBatch(failedEvents);
    }

    @Transactional
    public void cleanupOldEvents(int retentionDays) {
        Instant cutoffDate = Instant.now().minusSeconds(retentionDays * 24 * 60 * 60);
        int deletedCount = outboxRepository.deletePublishedEventsOlderThan(cutoffDate);
        log.info("Cleaned up {} old published events", deletedCount);
    }
}
