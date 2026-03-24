package com.goldtrading.backend.reports.service;

import com.goldtrading.backend.admin.dto.response.PagedDataResponse;
import com.goldtrading.backend.common.exception.BusinessException;
import com.goldtrading.backend.reports.dto.response.MonthlyProfitItemResponse;
import com.goldtrading.backend.reports.dto.response.ReportSummaryResponse;
import com.goldtrading.backend.reports.dto.response.StrategyDistributionItemResponse;
import com.goldtrading.backend.reports.dto.response.TradeListItemResponse;
import com.goldtrading.backend.mt5accounts.repository.MT5AccountRepository;
import com.goldtrading.backend.strategies.repository.StrategyRepository;
import com.goldtrading.backend.trades.domain.entity.Trade;
import com.goldtrading.backend.trades.repository.TradeRepository;
import com.goldtrading.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsService {
    private final UserRepository userRepository;
    private final MT5AccountRepository mt5AccountRepository;
    private final StrategyRepository strategyRepository;
    private final TradeRepository tradeRepository;

    public ReportSummaryResponse summary(String email) {
        return summary(email, null, null, null, null);
    }

    public ReportSummaryResponse summary(String email, String account, String accountNumber, LocalDateTime from, LocalDateTime to) {
        var user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        var accounts = mt5AccountRepository.findByUserId(user.getId());
        Set<String> accountNumbers = accounts.stream()
                .map(a -> a.getAccountNumber() == null ? null : a.getAccountNumber().trim())
                .filter(a -> a != null && !a.isEmpty())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (accountNumbers.isEmpty()) {
            return new ReportSummaryResponse(0, 0d, BigDecimal.ZERO, 0);
        }

        String requestedAccount = resolveRequestedAccount(account, accountNumber, accountNumbers);
        LocalDateTime normalizedFrom = from;
        LocalDateTime normalizedTo = to;
        if ((normalizedFrom == null) != (normalizedTo == null)) {
            throw new BusinessException("VALIDATION_ERROR", "from and to must be provided together");
        }
        if (normalizedFrom != null && normalizedTo != null && normalizedTo.isBefore(normalizedFrom)) {
            throw new BusinessException("VALIDATION_ERROR", "to must be greater than or equal to from");
        }

        List<Trade> trades = requestedAccount == null
                ? querySummaryByAccountSet(List.copyOf(accountNumbers), normalizedFrom, normalizedTo)
                : querySummaryBySingleAccount(List.copyOf(accountNumbers), requestedAccount, normalizedFrom, normalizedTo);
        long total = trades.size();
        long wins = trades.stream().filter(t -> t.getPnl() != null && t.getPnl().compareTo(BigDecimal.ZERO) >= 0).count();
        BigDecimal pnl = trades.stream().map(t -> t.getPnl() == null ? BigDecimal.ZERO : t.getPnl()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ReportSummaryResponse(total, total == 0 ? 0d : (wins * 100.0 / total), pnl, accounts.size());
    }

    public PagedDataResponse<TradeListItemResponse> trades(String email, int page, int pageSize, String sortBy, String sortOrder) {
        return trades(email, page, pageSize, sortBy, sortOrder, null, null, null, null);
    }

    public PagedDataResponse<TradeListItemResponse> trades(String email, int page, int pageSize, String sortBy, String sortOrder, String account, String accountNumber) {
        return trades(email, page, pageSize, sortBy, sortOrder, account, accountNumber, null, null);
    }

    public PagedDataResponse<TradeListItemResponse> trades(String email, int page, int pageSize, String sortBy, String sortOrder, String account, String accountNumber, LocalDateTime from, LocalDateTime to) {
        var user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        var accounts = mt5AccountRepository.findByUserId(user.getId());
        Set<String> accountNumbers = accounts.stream()
                .map(a -> a.getAccountNumber() == null ? null : a.getAccountNumber().trim())
                .filter(a -> a != null && !a.isEmpty())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (accountNumbers.isEmpty()) {
            return new PagedDataResponse<>(List.of(), page, pageSize, 0, 0);
        }
        var pageable = PageRequest.of(page, pageSize, "asc".equalsIgnoreCase(sortOrder) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());

        String requestedAccount = resolveRequestedAccount(account, accountNumber, accountNumbers);
        LocalDateTime normalizedFrom = from;
        LocalDateTime normalizedTo = to;
        if ((normalizedFrom == null) != (normalizedTo == null)) {
            throw new BusinessException("VALIDATION_ERROR", "from and to must be provided together");
        }
        if (normalizedFrom != null && normalizedTo != null && normalizedTo.isBefore(normalizedFrom)) {
            throw new BusinessException("VALIDATION_ERROR", "to must be greater than or equal to from");
        }

        var pg = requestedAccount == null
                ? queryByAccountSet(List.copyOf(accountNumbers), normalizedFrom, normalizedTo, pageable).map(this::toItem)
                : queryBySingleAccount(List.copyOf(accountNumbers), requestedAccount, normalizedFrom, normalizedTo, pageable).map(this::toItem);
        return PagedDataResponse.of(pg);
    }

    public List<MonthlyProfitItemResponse> monthlyProfit(String email, String account, String accountNumber, LocalDateTime from, LocalDateTime to) {
        var user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        var accounts = mt5AccountRepository.findByUserId(user.getId());
        Set<String> accountNumbers = accounts.stream()
                .map(a -> a.getAccountNumber() == null ? null : a.getAccountNumber().trim())
                .filter(a -> a != null && !a.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (accountNumbers.isEmpty()) {
            return List.of();
        }

        String requestedAccount = resolveRequestedAccount(account, accountNumber, accountNumbers);
        LocalDateTime[] range = validateAndNormalizeDateRange(from, to);
        LocalDateTime normalizedFrom = range[0];
        LocalDateTime normalizedTo = range[1];

        List<Trade> trades = requestedAccount == null
                ? querySummaryByAccountSet(List.copyOf(accountNumbers), normalizedFrom, normalizedTo)
                : querySummaryBySingleAccount(List.copyOf(accountNumbers), requestedAccount, normalizedFrom, normalizedTo);

        Map<YearMonth, BigDecimal> byMonth = trades.stream()
                .filter(t -> t.getOpenedAt() != null)
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getOpenedAt()),
                        Collectors.mapping(t -> t.getPnl() == null ? BigDecimal.ZERO : t.getPnl(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new MonthlyProfitItemResponse(e.getKey().toString(), e.getValue()))
                .toList();
    }

    public List<StrategyDistributionItemResponse> strategyDistribution(String email, String account, String accountNumber, LocalDateTime from, LocalDateTime to) {
        var user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        var accounts = mt5AccountRepository.findByUserId(user.getId());
        Set<String> accountNumbers = accounts.stream()
                .map(a -> a.getAccountNumber() == null ? null : a.getAccountNumber().trim())
                .filter(a -> a != null && !a.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (accountNumbers.isEmpty()) {
            return List.of();
        }

        String requestedAccount = resolveRequestedAccount(account, accountNumber, accountNumbers);
        LocalDateTime[] range = validateAndNormalizeDateRange(from, to);
        LocalDateTime normalizedFrom = range[0];
        LocalDateTime normalizedTo = range[1];

        List<Trade> trades = requestedAccount == null
                ? querySummaryByAccountSet(List.copyOf(accountNumbers), normalizedFrom, normalizedTo)
                : querySummaryBySingleAccount(List.copyOf(accountNumbers), requestedAccount, normalizedFrom, normalizedTo);

        if (trades.isEmpty()) {
            return List.of();
        }

        Map<String, UUID> accountToStrategy = accounts.stream()
                .filter(a -> a.getAccountNumber() != null && !a.getAccountNumber().isBlank() && a.getStrategyId() != null)
                .collect(Collectors.toMap(a -> a.getAccountNumber().trim(), a -> a.getStrategyId(), (left, right) -> left));
        Set<UUID> strategyIds = accountToStrategy.values().stream().filter(id -> id != null).collect(Collectors.toSet());
        Map<UUID, com.goldtrading.backend.strategies.domain.entity.Strategy> strategyMap = strategyIds.isEmpty()
                ? Map.of()
                : strategyRepository.findByIdIn(strategyIds).stream().collect(Collectors.toMap(s -> s.getId(), s -> s));

        long totalTrades = trades.size();
        Map<String, List<Trade>> grouped = trades.stream()
                .collect(Collectors.groupingBy(t -> resolveStrategyKeyForTrade(t, accountToStrategy)));

        return grouped.entrySet().stream()
                .map(e -> {
                    String key = e.getKey();
                    List<Trade> bucket = e.getValue();
                    long tradeCount = bucket.size();
                    BigDecimal profit = bucket.stream()
                            .map(t -> t.getPnl() == null ? BigDecimal.ZERO : t.getPnl())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    double percent = totalTrades == 0 ? 0d : (tradeCount * 100.0 / totalTrades);

                    if ("UNASSIGNED".equals(key)) {
                        return new StrategyDistributionItemResponse("UNASSIGNED", "Unassigned", tradeCount, profit, percent);
                    }

                    UUID id = UUID.fromString(key);
                    var strategy = strategyMap.get(id);
                    String code = strategy == null ? "UNKNOWN" : strategy.getCode();
                    String name = strategy == null ? "Unknown strategy" : strategy.getNameVi();
                    return new StrategyDistributionItemResponse(code, name, tradeCount, profit, percent);
                })
                .sorted((a, b) -> Long.compare(b.tradeCount(), a.tradeCount()))
                .toList();
    }

    private org.springframework.data.domain.Page<Trade> queryByAccountSet(List<String> accountNumbers, LocalDateTime from, LocalDateTime to, org.springframework.data.domain.Pageable pageable) {
        if (from == null) {
            return tradeRepository.findByAccountIn(accountNumbers, pageable);
        }
        return tradeRepository.findByAccountInAndOpenedAtBetween(accountNumbers, from, to, pageable);
    }

    private org.springframework.data.domain.Page<Trade> queryBySingleAccount(List<String> accountNumbers, String account, LocalDateTime from, LocalDateTime to, org.springframework.data.domain.Pageable pageable) {
        if (from == null) {
            return tradeRepository.findByAccountInAndAccount(accountNumbers, account, pageable);
        }
        return tradeRepository.findByAccountInAndAccountAndOpenedAtBetween(accountNumbers, account, from, to, pageable);
    }

    private List<Trade> querySummaryByAccountSet(List<String> accountNumbers, LocalDateTime from, LocalDateTime to) {
        if (from == null) {
            return tradeRepository.findByAccountIn(accountNumbers);
        }
        return tradeRepository.findByAccountInAndOpenedAtBetween(accountNumbers, from, to);
    }

    private List<Trade> querySummaryBySingleAccount(List<String> accountNumbers, String account, LocalDateTime from, LocalDateTime to) {
        if (from == null) {
            return tradeRepository.findByAccountInAndAccount(accountNumbers, account);
        }
        return tradeRepository.findByAccountInAndAccountAndOpenedAtBetween(accountNumbers, account, from, to);
    }

    private String resolveRequestedAccount(String account, String accountNumber, Set<String> ownedAccounts) {
        String normalizedAccount = normalizeAccountFilter(account);
        String normalizedAccountNumber = normalizeAccountFilter(accountNumber);

        if (normalizedAccount != null && normalizedAccountNumber != null
                && !normalizedAccount.equalsIgnoreCase(normalizedAccountNumber)) {
            throw new BusinessException("VALIDATION_ERROR", "account and accountNumber must match when both are provided");
        }

        String requested = normalizedAccount != null ? normalizedAccount : normalizedAccountNumber;
        if (requested == null) {
            return null;
        }
        for (String owned : ownedAccounts) {
            if (owned.equalsIgnoreCase(requested)) {
                return owned;
            }
        }

        throw new BusinessException("ACCESS_DENIED", "Account does not belong to user");
    }

    private String normalizeAccountFilter(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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

    private LocalDateTime[] validateAndNormalizeDateRange(LocalDateTime from, LocalDateTime to) {
        LocalDateTime normalizedFrom = from;
        LocalDateTime normalizedTo = to;
        if ((normalizedFrom == null) != (normalizedTo == null)) {
            throw new BusinessException("VALIDATION_ERROR", "from and to must be provided together");
        }
        if (normalizedFrom != null && normalizedTo != null && normalizedTo.isBefore(normalizedFrom)) {
            throw new BusinessException("VALIDATION_ERROR", "to must be greater than or equal to from");
        }
        return new LocalDateTime[]{normalizedFrom, normalizedTo};
    }

    private String resolveStrategyKeyForTrade(Trade trade, Map<String, UUID> accountToStrategy) {
        if (trade.getAccount() == null || trade.getAccount().isBlank()) {
            return "UNASSIGNED";
        }
        UUID strategyId = accountToStrategy.get(trade.getAccount().trim());
        return strategyId == null ? "UNASSIGNED" : strategyId.toString();
    }
}
