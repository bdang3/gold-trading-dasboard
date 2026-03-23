package com.goldtrading.backend.mt5accounts.dto;

import java.util.UUID;

public record BrokerCatalogItemResponse(UUID id, String code, String name) {
}
