package com.goldtrading.backend.mt5accounts.dto;

import java.util.UUID;

public record UpdateMt5AccountRequest(UUID strategyId, String timeframe, UUID riskRuleId, String broker, String server) {}

