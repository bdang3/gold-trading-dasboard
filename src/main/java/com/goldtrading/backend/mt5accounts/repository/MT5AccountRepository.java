package com.goldtrading.backend.mt5accounts.repository;

import com.goldtrading.backend.common.AccountStatus;
import com.goldtrading.backend.common.VerificationStatus;
import com.goldtrading.backend.mt5accounts.domain.entity.MT5Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface MT5AccountRepository extends JpaRepository<MT5Account, UUID>, JpaSpecificationExecutor<MT5Account> {
    List<MT5Account> findByUserId(UUID userId);
    boolean existsByAccountNumberIgnoreCase(String accountNumber);

    Page<MT5Account> findByDeletedAtIsNull(Pageable pageable);

    Page<MT5Account> findByDeletedAtIsNullAndStatusAndVerificationStatusAndBrokerContainingIgnoreCaseAndTimeframeContainingIgnoreCaseAndUserId(
            AccountStatus status, VerificationStatus verificationStatus, String broker, String timeframe, UUID userId, Pageable pageable);

    Page<MT5Account> findByDeletedAtIsNullAndAccountNumberContainingIgnoreCaseOrDeletedAtIsNullAndBrokerContainingIgnoreCase(
            String accountNumber, String broker, Pageable pageable);
}
