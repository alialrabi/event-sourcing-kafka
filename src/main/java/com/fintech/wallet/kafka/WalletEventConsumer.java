package com.fintech.wallet.kafka;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.fintech.wallet.event.WalletCreatedEvent;
import com.fintech.wallet.event.WalletCreditedEvent;
import com.fintech.wallet.event.WalletDebitedEvent;
import com.fintech.wallet.query.projection.WalletProjection;
import com.fintech.wallet.query.repository.ProcessedEvent;
import com.fintech.wallet.query.repository.ProcessedEventRepository;
import com.fintech.wallet.query.repository.WalletProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletEventConsumer {

    private final WalletProjectionRepository projectionRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${wallet.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consume(String message, Acknowledgment ack) {
        JsonNode node = objectMapper.readTree(message);
        String eventId = node.get("eventId").asText();
        String eventType = node.get("eventType").asText();

        // Idempotency check
        if (processedEventRepository.existsById(eventId)) {
            log.info("Event [{}] already processed, skipping", eventId);
            ack.acknowledge();
            return;
        }

        switch (eventType) {
            case "WALLET_CREATED" -> {
                WalletCreatedEvent e = objectMapper.readValue(message, WalletCreatedEvent.class);
                WalletProjection view = WalletProjection.builder()
                        .walletId(e.getWalletId())
                        .ownerId(e.getOwnerId())
                        .balance(e.getInitialBalance())
                        .createdAt(e.getOccurredAt())
                        .updatedAt(e.getOccurredAt())
                        .build();
                projectionRepository.save(view);
                log.info("Projection created for wallet [{}]", e.getWalletId());
            }
            case "WALLET_CREDITED" -> {
                WalletCreditedEvent e = objectMapper.readValue(message, WalletCreditedEvent.class);
                WalletProjection view = projectionRepository.findById(e.getWalletId()).orElseThrow();
                view.setBalance(view.getBalance().add(e.getAmount()));
                view.setUpdatedAt(e.getOccurredAt());
                projectionRepository.save(view);
                log.info("Projection credited for wallet [{}]", e.getWalletId());
            }
            case "WALLET_DEBITED" -> {
                WalletDebitedEvent e = objectMapper.readValue(message, WalletDebitedEvent.class);
                WalletProjection view = projectionRepository.findById(e.getWalletId()).orElseThrow();
                view.setBalance(view.getBalance().subtract(e.getAmount()));
                view.setUpdatedAt(e.getOccurredAt());
                projectionRepository.save(view);
                log.info("Projection debited for wallet [{}]", e.getWalletId());
            }
            default -> log.warn("Unknown event type: {}", eventType);
        }

        // Mark event as processed (same transaction as projection update)
        processedEventRepository.save(new ProcessedEvent(eventId, Instant.now()));

        // Commit offset only after successful processing
        ack.acknowledge();
    }
}

