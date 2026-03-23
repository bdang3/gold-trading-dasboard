package com.goldtrading.backend.infrastructure.runtime;

import com.goldtrading.backend.mt5accounts.domain.entity.MT5Account;

public interface BotRuntimeAdapter {
    RuntimeResult verify(MT5Account account);
    RuntimeResult start(MT5Account account);
    RuntimeResult stop(MT5Account account);

    record RuntimeResult(boolean success, int exitCode, String message) {}
}

