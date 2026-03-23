package com.goldtrading.backend.integration;

import com.goldtrading.backend.infrastructure.runtime.BotRuntimeAdapter;
import com.goldtrading.backend.mt5accounts.domain.entity.MT5Account;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
public class IntegrationTestConfig {

    @Bean
    @Primary
    public ControlledTestBotRuntimeAdapter controlledTestBotRuntimeAdapter() {
        return new ControlledTestBotRuntimeAdapter();
    }

    public static class ControlledTestBotRuntimeAdapter implements BotRuntimeAdapter {
        public enum Mode { SUCCESS, FAIL_START, FAIL_STOP, FAIL_VERIFY }

        private volatile Mode mode = Mode.SUCCESS;

        public void setMode(Mode mode) {
            this.mode = mode;
        }

        public void reset() {
            this.mode = Mode.SUCCESS;
        }

        @Override
        public RuntimeResult verify(MT5Account account) {
            if (mode == Mode.FAIL_VERIFY) return new RuntimeResult(false, 11, "verify failed (test)");
            return new RuntimeResult(true, 0, "verify success (test)");
        }

        @Override
        public RuntimeResult start(MT5Account account) {
            if (mode == Mode.FAIL_START) return new RuntimeResult(false, 21, "start failed (test)");
            return new RuntimeResult(true, 0, "start success (test)");
        }

        @Override
        public RuntimeResult stop(MT5Account account) {
            if (mode == Mode.FAIL_STOP) return new RuntimeResult(false, 31, "stop failed (test)");
            return new RuntimeResult(true, 0, "stop success (test)");
        }
    }
}
