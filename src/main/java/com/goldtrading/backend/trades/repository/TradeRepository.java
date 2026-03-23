package com.goldtrading.backend.trades.repository;

import com.goldtrading.backend.trades.domain.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByAccountIn(List<String> accounts);
    Page<Trade> findByAccountIn(List<String> accounts, Pageable pageable);
}
