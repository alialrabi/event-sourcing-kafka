package com.fintech.wallet.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WalletCreditedEvent extends WalletEvent {
    private BigDecimal amount;

    public WalletCreditedEvent(String walletId, BigDecimal amount) {
        super(walletId, Instant.now(), "WALLET_CREDITED");
        this.amount = amount;
    }
}

