package com.goldtrading.backend.reports.service;

import com.goldtrading.backend.admin.dto.response.PagedDataResponse;
import com.goldtrading.backend.reports.dto.response.ReportSummaryResponse;
import com.goldtrading.backend.reports.dto.response.TradeListItemResponse;
import com.goldtrading.backend.mt5accounts.repository.MT5AccountRepository;
import com.goldtrading.backend.trades.domain.entity.Trade;
import com.goldtrading.backend.trades.repository.TradeRepository;
import com.goldtrading.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportsService {
    private final UserRepository userRepository;
    private final MT5AccountRepository mt5AccountRepository;
    private final TradeRepository tradeRepository;

    public ReportSummaryResponse summary(String email) {
        var user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        var accounts = mt5AccountRepository.findByUserId(user.getId());
        var accountNumbers = accounts.stream().map(a -> a.getAccountNumber()).toList();
        List<Trade> trades = accountNumbers.isEmpty() ? List.of() : tradeRepository.findByAccountIn(accountNumbers);
        long total = trades.size();
        long wins = trades.stream().filter(t -> t.getPnl() != null && t.getPnl().compareTo(BigDecimal.ZERO) >= 0).count();
        BigDecimal pnl = trades.stream().map(t -> t.getPnl() == null ? BigDecimal.ZERO : t.getPnl()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ReportSummaryResponse(total, total == 0 ? 0d : (wins * 100.0 / total), pnl, accounts.size());
    }

    public PagedDataResponse<TradeListItemResponse> trades(String email, int page, int pageSize, String sortBy, String sortOrder) {
        var user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        var accounts = mt5AccountRepository.findByUserId(user.getId());
        var accountNumbers = accounts.stream().map(a -> a.getAccountNumber()).toList();
        if (accountNumbers.isEmpty()) {
            return new PagedDataResponse<>(List.of(), page, pageSize, 0, 0);
        }
        var pageable = PageRequest.of(page, pageSize, "asc".equalsIgnoreCase(sortOrder) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        var pg = tradeRepository.findByAccountIn(accountNumbers, pageable).map(this::toItem);
        return PagedDataResponse.of(pg);
    }

    private TradeListItemResponse toItem(Trade t) {
        return new TradeListItemResponse(
                t.getId(),
                t.getPositionId(),
                t.getSymbol(),
                t.getDirection(),
                t.getLots(),
                t.getEntryPrice(),
                t.getExitPrice(),
                t.getPnl(),
                t.getExitReason(),
                t.getOpenedAt(),
                t.getClosedAt(),
                t.getRunId(),
                t.getAccount(),
                t.getCreatedAt()
        );
    }
}
