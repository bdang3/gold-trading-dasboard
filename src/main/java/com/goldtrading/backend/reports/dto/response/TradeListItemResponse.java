package com.goldtrading.backend.reports.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeListItemResponse(
        Long id,
        Long positionId,
        String symbol,
        String direction,
        BigDecimal lots,
        BigDecimal entryPrice,
        BigDecimal exitPrice,
        BigDecimal pnl,
        String exitReason,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        String runId,
        String account,
        LocalDateTime createdAt
) {}
