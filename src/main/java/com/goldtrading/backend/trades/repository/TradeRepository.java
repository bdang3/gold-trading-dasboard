package com.goldtrading.backend.trades.repository;

import com.goldtrading.backend.trades.domain.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByAccountIn(List<String> accounts);
    List<Trade> findByAccountInAndAccount(List<String> accounts, String account);
    List<Trade> findByAccountInAndOpenedAtBetween(List<String> accounts, LocalDateTime from, LocalDateTime to);
    List<Trade> findByAccountInAndAccountAndOpenedAtBetween(List<String> accounts, String account, LocalDateTime from, LocalDateTime to);
    Page<Trade> findByAccountIn(List<String> accounts, Pageable pageable);
    Page<Trade> findByAccountInAndAccount(List<String> accounts, String account, Pageable pageable);
    Page<Trade> findByAccountInAndOpenedAtBetween(List<String> accounts, LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<Trade> findByAccountInAndAccountAndOpenedAtBetween(List<String> accounts, String account, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
