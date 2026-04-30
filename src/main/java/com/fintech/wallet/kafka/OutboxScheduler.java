package com.fintech.wallet.kafka;

import com.fintech.wallet.eventstore.OutboxEntity;
import com.fintech.wallet.eventstore.OutboxRepository;
import com.fintech.wallet.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${wallet.kafka.topic}")
    private String topic;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEntity> pending = outboxRepository.findByPublishedFalseOrderByCreatedAtAsc();

        for (OutboxEntity outbox : pending) {
            try {
                kafkaTemplate.send(topic, outbox.getAggregateId(), outbox.getPayload()).get();
                outbox.setPublished(true);
                outbox.setPublishedAt(Instant.now());
                outboxRepository.save(outbox);
                log.info("Outbox published event [{}] for aggregate [{}]", outbox.getEventType(), outbox.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event id [{}], will retry", outbox.getId(), e);
                throw new BusinessException("Failed to publish event" );
            }
        }
    }
}

