package com.enaya.product_service.infrastructure.messaging;

import com.enaya.product_service.infrastructure.persistence.outbox.OutboxEvent;
import com.enaya.product_service.infrastructure.persistence.outbox.OutboxEventRepository;
import com.enaya.product_service.infrastructure.persistence.outbox.OutboxEventStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final OutboxEventRepository outboxEventRepository;

    @Override
    public void publish(String topic, Object event) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType(event.getClass().getSimpleName())
                .topic(topic)
                .payload(event.toString())
                .aggregateId(extractAggregateId(event))
                .aggregateType(event.getClass().getSimpleName())
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .build();

        outboxEventRepository.save(outboxEvent);
        log.info("Event saved to outbox: {}", outboxEvent);
    }

    private String extractAggregateId(Object event) {
        // Implémentez la logique pour extraire l'ID de l'agrégat de l'événement
        // Par exemple, si l'événement a une méthode getId() ou un champ id
        try {
            return event.getClass().getMethod("getId").invoke(event).toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
} 