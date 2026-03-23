package com.goldtrading.backend.mt5accounts.dto;

import com.goldtrading.backend.common.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record CreateMt5AccountRequest(
        @NotBlank @Pattern(regexp = "^[0-9]+$") String accountNumber,
        @NotBlank String password,
        @NotNull UUID brokerId,
        @NotNull UUID serverId,
        @NotNull AccountType accountType
) {}

