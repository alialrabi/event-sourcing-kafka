package com.fintech.wallet.command.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditWalletRequest {
    @NotBlank
    private String walletId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}

