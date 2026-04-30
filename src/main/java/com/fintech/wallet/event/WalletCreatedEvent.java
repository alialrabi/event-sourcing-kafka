package com.fintech.wallet.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WalletCreatedEvent extends WalletEvent {
    private String ownerId;
    private BigDecimal initialBalance;

    public WalletCreatedEvent(String walletId, String ownerId, BigDecimal initialBalance) {
        super(walletId, Instant.now(), "WALLET_CREATED");
        this.ownerId = ownerId;
        this.initialBalance = initialBalance;
    }
}

