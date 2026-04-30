package com.fintech.wallet.command.domain;

import com.fintech.wallet.event.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class Wallet {
    private String walletId;
    private String ownerId;
    private BigDecimal balance;
    private List<WalletEvent> uncommittedEvents = new ArrayList<>();

    public static Wallet reconstitute(List<WalletEvent> events) {
        Wallet wallet = new Wallet();
        events.forEach(wallet::apply);
        return wallet;
    }

    public static Wallet create(String walletId, String ownerId, BigDecimal initialBalance) {
        Wallet wallet = new Wallet();
        WalletCreatedEvent event = new WalletCreatedEvent(walletId, ownerId, initialBalance);
        wallet.apply(event);
        wallet.uncommittedEvents.add(event);
        return wallet;
    }

    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        WalletCreditedEvent event = new WalletCreditedEvent(walletId, amount);
        apply(event);
        uncommittedEvents.add(event);
    }

    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (amount.compareTo(balance) > 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        WalletDebitedEvent event = new WalletDebitedEvent(walletId, amount);
        apply(event);
        uncommittedEvents.add(event);
    }

    private void apply(WalletEvent event) {
        if (event instanceof WalletCreatedEvent e) {
            this.walletId = e.getWalletId();
            this.ownerId = e.getOwnerId();
            this.balance = e.getInitialBalance();
        } else if (event instanceof WalletCreditedEvent e) {
            this.balance = this.balance.add(e.getAmount());
        } else if (event instanceof WalletDebitedEvent e) {
            this.balance = this.balance.subtract(e.getAmount());
        }
    }
}

