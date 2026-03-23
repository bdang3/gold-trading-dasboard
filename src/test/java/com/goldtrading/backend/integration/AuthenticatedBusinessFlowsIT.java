package com.goldtrading.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.goldtrading.backend.common.*;
import com.goldtrading.backend.mt5accounts.domain.entity.MT5Account;
import com.goldtrading.backend.ports.domain.entity.PortMaster;
import com.goldtrading.backend.users.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticatedBusinessFlowsIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void cannotModifyMt5ConfigWhileProcessing() throws Exception {
        User user = createUser(RoleType.USER, "modify");
        MT5Account account = createAccount(user, AccountStatus.PROCESSING, VerificationStatus.VERIFIED, AdminActionState.PROCESSING);
        var strategy = createStrategy("S");

        var res = mockMvc.perform(patch("/api/v1/mt5-accounts/" + account.getId())
                        .header(authHeaderName(), bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"strategyId\":\"" + strategy.getId() + "\",\"timeframe\":\"H1\"}"))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode body = json(res.getResponse().getContentAsString());
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("code").asText()).isEqualTo("ACCOUNT_ALREADY_PROCESSING");

        MT5Account reloaded = mt5AccountRepository.findById(account.getId()).orElseThrow();
        assertThat(reloaded.getTimeframe()).isNull();
        assertThat(reloaded.getStrategyId()).isNull();
    }

    @Test
    void cannotDeleteProcessingAccount() throws Exception {
        User user = createUser(RoleType.USER, "delete");
        MT5Account account = createAccount(user, AccountStatus.PROCESSING, VerificationStatus.VERIFIED, AdminActionState.PROCESSING);

        var res = mockMvc.perform(delete("/api/v1/mt5-accounts/" + account.getId())
                        .header(authHeaderName(), bearer(user)))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode body = json(res.getResponse().getContentAsString());
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("code").asText()).isEqualTo("ACCOUNT_ALREADY_PROCESSING");

        MT5Account reloaded = mt5AccountRepository.findById(account.getId()).orElseThrow();
        assertThat(reloaded.getDeletedAt()).isNull();
    }

    @Test
    void cannotAssignOccupiedPort() throws Exception {
        User admin = createUser(RoleType.ADMIN, "admin-assign");
        User owner1 = createUser(RoleType.USER, "owner1");
        User owner2 = createUser(RoleType.USER, "owner2");
        MT5Account occupiedBy = createAccount(owner1, AccountStatus.PROCESSING, VerificationStatus.VERIFIED, AdminActionState.PROCESSING);
        MT5Account target = createAccount(owner2, AccountStatus.PENDING, VerificationStatus.VERIFIED, AdminActionState.PENDING_ADMIN);

        PortMaster port = createPort(PortStatus.OCCUPIED);
        port.setCurrentMt5AccountId(occupiedBy.getId());
        portMasterRepository.save(port);

        var res = mockMvc.perform(post("/api/v1/admin/mt5-accounts/" + target.getId() + "/assign-port")
                        .header(authHeaderName(), bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"portId\":\"" + port.getId() + "\"}"))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode body = json(res.getResponse().getContentAsString());
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("code").asText()).isEqualTo("PORT_OCCUPIED");

        MT5Account tReload = mt5AccountRepository.findById(target.getId()).orElseThrow();
        PortMaster pReload = portMasterRepository.findById(port.getId()).orElseThrow();
        assertThat(tReload.getAssignedPortId()).isNull();
        assertThat(pReload.getCurrentMt5AccountId()).isEqualTo(occupiedBy.getId());
        assertThat(pReload.getStatus()).isEqualTo(PortStatus.OCCUPIED);
    }

    @Test
    void startFailureRollsBackPortAssignment() throws Exception {
        controlledAdapter.setMode(IntegrationTestConfig.ControlledTestBotRuntimeAdapter.Mode.FAIL_START);

        User admin = createUser(RoleType.ADMIN, "admin-start");
        User owner = createUser(RoleType.USER, "owner-start");
        var strategy = createStrategy("ST");
        var risk = createRiskRule("RR");
        MT5Account account = createAccount(owner, AccountStatus.PENDING, VerificationStatus.VERIFIED, AdminActionState.PENDING_ADMIN);
        account.setStrategyId(strategy.getId());
        account.setRiskRuleId(risk.getId());
        account.setTimeframe("M5");
        account = mt5AccountRepository.save(account);
        final java.util.UUID accountId = account.getId();

        PortMaster port = createPort(PortStatus.AVAILABLE);

        mockMvc.perform(post("/api/v1/admin/mt5-accounts/" + account.getId() + "/assign-port")
                        .header(authHeaderName(), bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"portId\":\"" + port.getId() + "\"}"))
                .andExpect(status().isOk());

        var res = mockMvc.perform(post("/api/v1/admin/mt5-accounts/" + account.getId() + "/start")
                        .header(authHeaderName(), bearer(admin)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = json(res.getResponse().getContentAsString());
        assertThat(body.get("success").asBoolean()).isTrue();

        MT5Account reloaded = mt5AccountRepository.findById(account.getId()).orElseThrow();
        PortMaster pReload = portMasterRepository.findById(port.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(AccountStatus.FAILED);
        assertThat(reloaded.getAdminAction()).isEqualTo(AdminActionState.PENDING_ADMIN);
        assertThat(reloaded.getAssignedPortId()).isNull();
        assertThat(pReload.getStatus()).isEqualTo(PortStatus.AVAILABLE);
        assertThat(pReload.getCurrentMt5AccountId()).isNull();

        assertThat(processLogRepository.findAll().stream().anyMatch(l -> l.getMt5AccountId().equals(accountId) && "START".equals(l.getActionType()) && "failed".equalsIgnoreCase(l.getResult()))).isTrue();
        assertThat(auditLogRepository.findAll().stream().anyMatch(l -> "START_BOT".equals(l.getAction()))).isTrue();
        assertThat(notificationRepository.findByUserIdOrderByCreatedAtDesc(owner.getId()).stream().anyMatch(n -> n.getTitle().toLowerCase().contains("start failure") || n.getType().equalsIgnoreCase("error"))).isTrue();
    }

    @Test
    void stopReleasesPort() throws Exception {
        controlledAdapter.setMode(IntegrationTestConfig.ControlledTestBotRuntimeAdapter.Mode.SUCCESS);

        User admin = createUser(RoleType.ADMIN, "admin-stop");
        User owner = createUser(RoleType.USER, "owner-stop");
        MT5Account account = createAccount(owner, AccountStatus.PROCESSING, VerificationStatus.VERIFIED, AdminActionState.PROCESSING);
        PortMaster port = createPort(PortStatus.OCCUPIED);
        port.setCurrentMt5AccountId(account.getId());
        port = portMasterRepository.save(port);
        account.setAssignedPortId(port.getId());
        account = mt5AccountRepository.save(account);
        final java.util.UUID accountId = account.getId();

        var res = mockMvc.perform(post("/api/v1/admin/mt5-accounts/" + account.getId() + "/stop")
                        .header(authHeaderName(), bearer(admin)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = json(res.getResponse().getContentAsString());
        assertThat(body.get("success").asBoolean()).isTrue();

        MT5Account reloaded = mt5AccountRepository.findById(account.getId()).orElseThrow();
        PortMaster pReload = portMasterRepository.findById(port.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(AccountStatus.STOPPED);
        assertThat(reloaded.getStoppedAt()).isNotNull();
        assertThat(reloaded.getAssignedPortId()).isNull();
        assertThat(pReload.getStatus()).isEqualTo(PortStatus.AVAILABLE);
        assertThat(pReload.getCurrentMt5AccountId()).isNull();

        assertThat(processLogRepository.findAll().stream().anyMatch(l -> l.getMt5AccountId().equals(accountId) && "STOP".equals(l.getActionType()) && "success".equalsIgnoreCase(l.getResult()))).isTrue();
        assertThat(auditLogRepository.findAll().stream().anyMatch(l -> "STOP_BOT".equals(l.getAction()))).isTrue();
        assertThat(notificationRepository.findByUserIdOrderByCreatedAtDesc(owner.getId()).stream().anyMatch(n -> n.getTitle().toLowerCase().contains("dừng") || n.getMessage().toLowerCase().contains("đã dừng"))).isTrue();
    }

    @Test
    void notificationMarkReadOwnershipSafe() throws Exception {
        User userA = createUser(RoleType.USER, "user-a");
        User userB = createUser(RoleType.USER, "user-b");
        var notifB = createNotification(userB);

        var res = mockMvc.perform(post("/api/v1/notifications/" + notifB.getId() + "/read")
                        .header(authHeaderName(), bearer(userA)))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode body = json(res.getResponse().getContentAsString());
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("code").asText()).isEqualTo("ACCESS_DENIED");

        var reloaded = notificationRepository.findById(notifB.getId()).orElseThrow();
        assertThat(reloaded.getReadAt()).isNull();
    }
}
