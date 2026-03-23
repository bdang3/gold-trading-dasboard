package com.goldtrading.backend.ports.repository;

import com.goldtrading.backend.ports.domain.entity.PortMaster;
import com.goldtrading.backend.common.PortStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PortMasterRepository extends JpaRepository<PortMaster, UUID> {
    List<PortMaster> findByIdIn(Collection<UUID> ids);
    List<PortMaster> findByStatus(PortStatus status);
}

