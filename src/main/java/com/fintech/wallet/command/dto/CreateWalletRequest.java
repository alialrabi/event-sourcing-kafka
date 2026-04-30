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
public class CreateWalletRequest {
    @NotBlank
    private String ownerId;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal initialBalance;
}

