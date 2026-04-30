package com.fintech.wallet.command.application;

import tools.jackson.databind.ObjectMapper;
import com.fintech.wallet.command.domain.Wallet;
import com.fintech.wallet.command.dto.CreateWalletRequest;
import com.fintech.wallet.command.dto.CreditWalletRequest;
import com.fintech.wallet.command.dto.DebitWalletRequest;
import com.fintech.wallet.event.*;
import com.fintech.wallet.exception.BusinessException;
import com.fintech.wallet.eventstore.AggregateType;
import com.fintech.wallet.eventstore.EventEntity;
import com.fintech.wallet.eventstore.EventStoreRepository;
import com.fintech.wallet.eventstore.OutboxEntity;
import com.fintech.wallet.eventstore.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletCommandService {

    private final EventStoreRepository eventStoreRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public String createWallet(CreateWalletRequest cmd) {
        String walletId = UUID.randomUUID().toString();
        Wallet wallet = Wallet.create(walletId, cmd.getOwnerId(), cmd.getInitialBalance());
        saveAndPublish(wallet);
        return walletId;
    }

    public void creditWallet(CreditWalletRequest cmd) {
        Wallet wallet = loadWallet(cmd.getWalletId());
        wallet.credit(cmd.getAmount());
        saveAndPublish(wallet);
    }

    public void debitWallet(DebitWalletRequest cmd) {
        Wallet wallet = loadWallet(cmd.getWalletId());
        wallet.debit(cmd.getAmount());
        saveAndPublish(wallet);
    }

    private Wallet loadWallet(String walletId) {
        List<EventEntity> entities = eventStoreRepository
                .findByAggregateIdOrderByVersionAsc(walletId);
        if (entities.isEmpty()) {
            throw new BusinessException("Wallet not found: " + walletId);
        }
        List<WalletEvent> events = new ArrayList<>();
        for (EventEntity entity : entities) {
            events.add(deserialize(entity));
        }
        return Wallet.reconstitute(events);
    }

    private void saveAndPublish(Wallet wallet) {
        long version = eventStoreRepository
                .findByAggregateIdOrderByVersionAsc(wallet.getWalletId()).size();

        for (WalletEvent event : wallet.getUncommittedEvents()) {
            try {
                String payload = objectMapper.writeValueAsString(event);

                EventEntity entity = EventEntity.builder()
                        .aggregateId(wallet.getWalletId())
                        .aggregateType(AggregateType.WALLET)
                        .eventType(event.getEventType())
                        .payload(payload)
                        .occurredAt(event.getOccurredAt())
                        .version(++version)
                        .build();
                eventStoreRepository.save(entity);

                OutboxEntity outbox = OutboxEntity.builder()
                        .aggregateId(wallet.getWalletId())
                        .eventType(event.getEventType())
                        .payload(payload)
                        .createdAt(event.getOccurredAt())
                        .published(false)
                        .build();
                outboxRepository.save(outbox);
            } catch (Exception e) {
                throw new BusinessException("Failed to save event", e);
            }
        }
        wallet.getUncommittedEvents().clear();
    }

    private WalletEvent deserialize(EventEntity entity) {
        try {
            return switch (entity.getEventType()) {
                case "WALLET_CREATED" -> objectMapper.readValue(entity.getPayload(), WalletCreatedEvent.class);
                case "WALLET_CREDITED" -> objectMapper.readValue(entity.getPayload(), WalletCreditedEvent.class);
                case "WALLET_DEBITED" -> objectMapper.readValue(entity.getPayload(), WalletDebitedEvent.class);
                default -> throw new BusinessException("Unknown event type: " + entity.getEventType());
            };
        } catch (Exception e) {
            throw new BusinessException("Failed to deserialize event", e);
        }
    }
}
