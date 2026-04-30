package com.fintech.wallet.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WalletDebitedEvent extends WalletEvent {
    private BigDecimal amount;

    public WalletDebitedEvent(String walletId, BigDecimal amount) {
        super(walletId, Instant.now(), "WALLET_DEBITED");
        this.amount = amount;
    }
}

