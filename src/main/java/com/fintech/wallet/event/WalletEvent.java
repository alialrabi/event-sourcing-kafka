package com.fintech.wallet.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class WalletEvent {
    private String walletId;
    private Instant occurredAt;
    private String eventType;
}

