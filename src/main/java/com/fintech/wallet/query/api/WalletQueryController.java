package com.fintech.wallet.query.api;

import com.fintech.wallet.query.projection.WalletProjection;
import com.fintech.wallet.query.repository.WalletProjectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets/queries")
@RequiredArgsConstructor
public class WalletQueryController {

    private final WalletProjectionRepository projectionRepository;

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletProjection> getWallet(@PathVariable String walletId) {
        return projectionRepository.findById(walletId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<WalletProjection>> getByOwner(@PathVariable String ownerId) {
        return ResponseEntity.ok(projectionRepository.findByOwnerId(ownerId));
    }

}
