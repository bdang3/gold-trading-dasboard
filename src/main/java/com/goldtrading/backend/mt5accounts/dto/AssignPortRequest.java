package com.goldtrading.backend.mt5accounts.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignPortRequest(@NotNull UUID portId) {}

