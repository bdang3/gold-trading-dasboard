package com.goldtrading.backend.mt5accounts.policy;

import com.goldtrading.backend.common.PortStatus;
import com.goldtrading.backend.common.exception.BusinessException;
import com.goldtrading.backend.ports.domain.entity.PortMaster;
import org.springframework.stereotype.Component;

@Component
public class PortLifecyclePolicy {
    public void ensureCanAssign(PortMaster port) {
        if (port.getStatus() == PortStatus.OCCUPIED) throw new BusinessException("PORT_OCCUPIED", "Port is occupied");
        if (port.getStatus() == PortStatus.DISABLED) throw new BusinessException("PORT_DISABLED", "Port is disabled");
    }
}
