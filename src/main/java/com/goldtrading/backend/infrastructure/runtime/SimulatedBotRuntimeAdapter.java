package com.goldtrading.backend.infrastructure.runtime;

import com.goldtrading.backend.mt5accounts.domain.entity.MT5Account;
import org.springframework.stereotype.Component;

@Component
public class SimulatedBotRuntimeAdapter implements BotRuntimeAdapter {
    @Override
    public RuntimeResult verify(MT5Account account) {
        boolean ok = !account.getAccountNumber().endsWith("9");
        return ok ? new RuntimeResult(true, 0, "Verification success") : new RuntimeResult(false, 1, "Verification failed");
    }

    @Override
    public RuntimeResult start(MT5Account account) {
        boolean ok = !account.getAccountNumber().endsWith("8");
        return ok ? new RuntimeResult(true, 0, "Bot started") : new RuntimeResult(false, 2, "Failed to start bot process");
    }

    @Override
    public RuntimeResult stop(MT5Account account) {
        boolean ok = !account.getAccountNumber().endsWith("7");
        return ok ? new RuntimeResult(true, 0, "Bot stopped") : new RuntimeResult(false, 3, "Failed to stop process");
    }
}

