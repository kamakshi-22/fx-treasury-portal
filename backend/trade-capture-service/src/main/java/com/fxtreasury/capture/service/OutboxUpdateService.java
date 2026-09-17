package com.fxtreasury.capture.service;

import com.fxtreasury.capture.entity.OutboxEntity;
import com.fxtreasury.capture.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxUpdateService {
    private final OutboxRepository outboxRepository;

    public OutboxUpdateService(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsPublished(OutboxEntity event) {
        event.setStatus("PUBLISHED");
        outboxRepository.save(event);
    }
}