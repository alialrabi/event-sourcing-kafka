package com.fintech.wallet.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class WalletEvent {
    private String eventId;
    private String walletId;
    private Instant occurredAt;
    private String eventType;

    protected WalletEvent(String walletId, Instant occurredAt, String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.walletId = walletId;
        this.occurredAt = occurredAt;
        this.eventType = eventType;
    }
}

