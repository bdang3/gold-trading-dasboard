package com.goldtrading.backend.mt5accounts.dto;

import java.util.UUID;

public record BrokerServerCatalogItemResponse(UUID id, UUID brokerId, String code, String name) {
}
