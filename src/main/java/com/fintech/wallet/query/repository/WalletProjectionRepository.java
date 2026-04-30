package com.fintech.wallet.query.repository;

import com.fintech.wallet.query.projection.WalletProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletProjectionRepository extends JpaRepository<WalletProjection, String> {
    List<WalletProjection> findByOwnerId(String ownerId);
}

