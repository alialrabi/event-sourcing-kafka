package com.fintech.wallet.command.api;

import com.fintech.wallet.command.application.WalletCommandService;
import com.fintech.wallet.command.dto.CreateWalletRequest;
import com.fintech.wallet.command.dto.CreditWalletRequest;
import com.fintech.wallet.command.dto.DebitWalletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets/commands")
@RequiredArgsConstructor
public class WalletCommandController {

    private final WalletCommandService commandService;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody @Valid CreateWalletRequest cmd) {
        String walletId = commandService.createWallet(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(walletId);
    }

    @PostMapping("/credit")
    public ResponseEntity<Void> credit(@RequestBody @Valid CreditWalletRequest cmd) {
        commandService.creditWallet(cmd);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/debit")
    public ResponseEntity<Void> debit(@RequestBody @Valid DebitWalletRequest cmd) {
        commandService.debitWallet(cmd);
        return ResponseEntity.ok().build();
    }
}
