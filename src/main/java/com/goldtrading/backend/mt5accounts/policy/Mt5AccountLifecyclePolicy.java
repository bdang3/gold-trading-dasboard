package com.goldtrading.backend.mt5accounts.policy;

import com.goldtrading.backend.common.*;
import com.goldtrading.backend.common.exception.BusinessException;
import com.goldtrading.backend.mt5accounts.domain.entity.MT5Account;
import org.springframework.stereotype.Component;

@Component
public class Mt5AccountLifecyclePolicy {
    public void ensureCanModifyConfig(MT5Account account) {
        if (account.getStatus() == AccountStatus.PROCESSING) {
            throw new BusinessException("ACCOUNT_ALREADY_PROCESSING", "Stop bot before changing config");
        }
    }

    public void ensureCanDelete(MT5Account account) {
        if (account.getStatus() == AccountStatus.PROCESSING) {
            throw new BusinessException("ACCOUNT_ALREADY_PROCESSING", "Cannot delete while processing");
        }
    }

    public void ensureCanStart(MT5Account account) {
        if (account.getVerificationStatus() != VerificationStatus.VERIFIED) throw new BusinessException("ACCOUNT_NOT_VERIFIED", "Account must be verified");
        if (account.getStrategyId() == null || account.getTimeframe() == null || account.getRiskRuleId() == null) {
            throw new BusinessException("VALIDATION_ERROR", "Missing strategy/timeframe/riskRule");
        }
        if (account.getStatus() == AccountStatus.PROCESSING) throw new BusinessException("ACCOUNT_ALREADY_PROCESSING", "Already processing");
    }

    public void ensureCanStop(MT5Account account) {
        if (account.getStatus() != AccountStatus.PROCESSING) {
            throw new BusinessException("INVALID_STATUS_TRANSITION", "Account not processing");
        }
    }
}
