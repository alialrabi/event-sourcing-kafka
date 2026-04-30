package com.fintech.wallet.query.projection;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "wallet_view")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletProjection {

    @Id
    private String walletId;
    private String ownerId;
    private BigDecimal balance;
    private Instant createdAt;
    private Instant updatedAt;
}

