package com.fintech.wallet.kafka;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.fintech.wallet.event.WalletCreatedEvent;
import com.fintech.wallet.event.WalletCreditedEvent;
import com.fintech.wallet.event.WalletDebitedEvent;
import com.fintech.wallet.query.projection.WalletProjection;
import com.fintech.wallet.query.repository.WalletProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletEventConsumer {

    private final WalletProjectionRepository projectionRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${wallet.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.get("eventType").asText();

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
        } catch (Exception ex) {
            log.error("Error processing wallet event", ex);
        }
    }
}

