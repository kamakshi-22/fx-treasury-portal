package com.fxtreasury.capture.service;

import com.fxtreasury.capture.entity.OutboxEntity;
import com.fxtreasury.capture.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j //private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
@Component
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final OutboxUpdateService outboxUpdateService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String tradeTopic;

    public OutboxPublisher(OutboxRepository outboxRepository,
                           OutboxUpdateService outboxUpdateService,
                           KafkaTemplate<String, String> kafkaTemplate,
                           @Value("${app.kafka.topics.trades-raw}") String tradeTopic) {
        this.outboxRepository = outboxRepository;
        this.outboxUpdateService = outboxUpdateService;
        this.kafkaTemplate = kafkaTemplate;
        this.tradeTopic = tradeTopic;
    }

    @Scheduled(fixedDelay = 500) //runs the method in background with 500ms delay after every execution
    public void publishPendingEvents() {
        List<OutboxEntity> pendingEvents = outboxRepository.findTop50PendingEvents();
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEntity event : pendingEvents) {
            try {
                // Network call outside any DB transaction
                //get() for synchronous call to kafka; stop & wait for response
                kafkaTemplate.send(tradeTopic, event.getAggregateId(), event.getPayload()).get();
                // Persisted immediately in its own isolated transaction
                outboxUpdateService.markAsPublished(event);
                log.info("Published outbox event ID {} for trade {}", event.getId(), event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
                break; //stop processing remaining events
            }
        }
    }
}