package com.goldtrading.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.goldtrading.backend.common.*;
import com.goldtrading.backend.mt5accounts.domain.entity.MT5Account;
import com.goldtrading.backend.ports.domain.entity.PortMaster;
import com.goldtrading.backend.trades.domain.entity.Trade;
import com.goldtrading.backend.trades.repository.TradeRepository;
import com.goldtrading.backend.users.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticatedBusinessFlowsIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    TradeRepository tradeRepository;

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

    @Test
    void tradeHistoryFiltersByAccountBeforePagination() throws Exception {
        User user = createUser(RoleType.USER, "trade-filter");
        MT5Account accountA = createAccount(user, AccountStatus.PENDING, VerificationStatus.VERIFIED, AdminActionState.PENDING_ADMIN);
        MT5Account accountB = createAccount(user, AccountStatus.PENDING, VerificationStatus.VERIFIED, AdminActionState.PENDING_ADMIN);

        accountA.setAccountNumber("700001");
        accountB.setAccountNumber("700002");
        accountA = mt5AccountRepository.save(accountA);
        accountB = mt5AccountRepository.save(accountB);

        LocalDateTime now = LocalDateTime.now();
        createTrade(accountA.getAccountNumber(), now.plusMinutes(3), BigDecimal.valueOf(10));
        createTrade(accountA.getAccountNumber(), now.plusMinutes(2), BigDecimal.valueOf(20));
        createTrade(accountA.getAccountNumber(), now.plusMinutes(1), BigDecimal.valueOf(30));
        createTrade(accountB.getAccountNumber(), now.minusMinutes(1), BigDecimal.valueOf(40));
        createTrade(accountB.getAccountNumber(), now.minusMinutes(2), BigDecimal.valueOf(50));
        createTrade(accountB.getAccountNumber(), now.minusMinutes(3), BigDecimal.valueOf(60));

        var filteredRes = mockMvc.perform(get("/api/v1/trades/my")
                        .header(authHeaderName(), bearer(user))
                        .param("account", "700002")
                        .param("page", "0")
                        .param("pageSize", "2")
                        .param("sortBy", "openedAt")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andReturn();

        var filteredData = json(filteredRes.getResponse().getContentAsString()).path("data");
        assertThat(filteredData.path("totalItems").asLong()).isEqualTo(3L);
        assertThat(filteredData.path("page").asInt()).isEqualTo(0);
        assertThat(filteredData.path("pageSize").asInt()).isEqualTo(2);
        assertThat(filteredData.path("items").size()).isEqualTo(2);
        for (var item : filteredData.path("items")) {
            assertThat(item.path("account").asText()).isEqualTo("700002");
        }
    }

    @Test
    void tradeHistoryRejectsNonOwnedAccountFilter() throws Exception {
        User user = createUser(RoleType.USER, "trade-invalid-filter");
        MT5Account owned = createAccount(user, AccountStatus.PENDING, VerificationStatus.VERIFIED, AdminActionState.PENDING_ADMIN);
        owned.setAccountNumber("700001");
        mt5AccountRepository.save(owned);

        var res = mockMvc.perform(get("/api/v1/trades/my")
                        .header(authHeaderName(), bearer(user))
                        .param("account", "799999")
                        .param("page", "0")
                        .param("pageSize", "20"))
                .andExpect(status().isBadRequest())
                .andReturn();

        var body = json(res.getResponse().getContentAsString());
        assertThat(body.path("success").asBoolean()).isFalse();
        assertThat(body.path("code").asText()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void tradeHistoryFiltersByInclusiveDateRange() throws Exception {
        User user = createUser(RoleType.USER, "trade-date-filter");
        MT5Account account = createAccount(user, AccountStatus.PENDING, VerificationStatus.VERIFIED, AdminActionState.PENDING_ADMIN);
        account.setAccountNumber("700003");
        account = mt5AccountRepository.save(account);

        createTrade(account.getAccountNumber(), LocalDateTime.of(2026, 2, 22, 23, 59, 59), BigDecimal.valueOf(1));
        createTrade(account.getAccountNumber(), LocalDateTime.of(2026, 2, 23, 0, 0, 0), BigDecimal.valueOf(2));
        createTrade(account.getAccountNumber(), LocalDateTime.of(2026, 2, 24, 12, 0, 0), BigDecimal.valueOf(3));
        createTrade(account.getAccountNumber(), LocalDateTime.of(2026, 2, 25, 23, 59, 59), BigDecimal.valueOf(4));
        createTrade(account.getAccountNumber(), LocalDateTime.of(2026, 2, 26, 0, 0, 0), BigDecimal.valueOf(5));

        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        var res = mockMvc.perform(get("/api/v1/trades/my")
                        .header(authHeaderName(), bearer(user))
                        .param("account", "700003")
                        .param("from", LocalDateTime.of(2026, 2, 23, 0, 0, 0, 0).format(fmt))
                        .param("to", LocalDateTime.of(2026, 2, 25, 23, 59, 59, 999_000_000).format(fmt))
                        .param("page", "0")
                        .param("pageSize", "20")
                        .param("sortBy", "openedAt")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andReturn();

        var data = json(res.getResponse().getContentAsString()).path("data");
        assertThat(data.path("totalItems").asLong()).isEqualTo(3L);
        for (var item : data.path("items")) {
            assertThat(item.path("account").asText()).isEqualTo("700003");
            String openedAt = item.path("openedAt").asText();
            assertThat(openedAt.compareTo("2026-02-23T00:00:00")).isGreaterThanOrEqualTo(0);
            assertThat(openedAt.compareTo("2026-02-25T23:59:59")).isLessThanOrEqualTo(0);
        }
    }

    @Test
    void reportsMonthlyProfitAndStrategyDistributionReturnData() throws Exception {
        User user = createUser(RoleType.USER, "reports-breakdown");
        var strategyA = createStrategy("SMA");
        var strategyB = createStrategy("EMA");

        MT5Account accountA = createAccount(user, AccountStatus.PENDING, VerificationStatus.VERIFIED, AdminActionState.PENDING_ADMIN);
        MT5Account accountB = createAccount(user, AccountStatus.PENDING, VerificationStatus.VERIFIED, AdminActionState.PENDING_ADMIN);
        accountA.setAccountNumber("710001");
        accountB.setAccountNumber("710002");
        accountA.setStrategyId(strategyA.getId());
        accountB.setStrategyId(strategyB.getId());
        accountA = mt5AccountRepository.save(accountA);
        accountB = mt5AccountRepository.save(accountB);

        createTrade(accountA.getAccountNumber(), LocalDateTime.of(2026, 1, 15, 10, 0, 0), BigDecimal.valueOf(100));
        createTrade(accountA.getAccountNumber(), LocalDateTime.of(2026, 2, 15, 10, 0, 0), BigDecimal.valueOf(-20));
        createTrade(accountB.getAccountNumber(), LocalDateTime.of(2026, 2, 20, 10, 0, 0), BigDecimal.valueOf(40));

        var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        var monthlyRes = mockMvc.perform(get("/api/v1/reports/my/monthly-profit")
                        .header(authHeaderName(), bearer(user))
                        .param("from", LocalDateTime.of(2026, 1, 1, 0, 0, 0, 0).format(fmt))
                        .param("to", LocalDateTime.of(2026, 2, 28, 23, 59, 59, 999_000_000).format(fmt)))
                .andExpect(status().isOk())
                .andReturn();

        var monthlyData = json(monthlyRes.getResponse().getContentAsString()).path("data");
        assertThat(monthlyData.size()).isEqualTo(2);
        assertThat(monthlyData.get(0).path("month").asText()).isEqualTo("2026-01");
        assertThat(monthlyData.get(0).path("profit").asDouble()).isEqualTo(100.0);
        assertThat(monthlyData.get(1).path("month").asText()).isEqualTo("2026-02");
        assertThat(monthlyData.get(1).path("profit").asDouble()).isEqualTo(20.0);

        var strategyRes = mockMvc.perform(get("/api/v1/reports/my/strategy-distribution")
                        .header(authHeaderName(), bearer(user))
                        .param("from", LocalDateTime.of(2026, 1, 1, 0, 0, 0, 0).format(fmt))
                        .param("to", LocalDateTime.of(2026, 2, 28, 23, 59, 59, 999_000_000).format(fmt)))
                .andExpect(status().isOk())
                .andReturn();

        var strategyData = json(strategyRes.getResponse().getContentAsString()).path("data");
        assertThat(strategyData.size()).isEqualTo(2);
        assertThat(strategyData.get(0).path("tradeCount").asLong()).isEqualTo(2L);
        assertThat(strategyData.get(1).path("tradeCount").asLong()).isEqualTo(1L);
    }

    private void createTrade(String account, LocalDateTime openedAt, BigDecimal pnl) {
        Trade trade = new Trade();
        trade.setPositionId(Math.abs(java.util.UUID.randomUUID().getMostSignificantBits()));
        trade.setSymbol("XAUUSD");
        trade.setDirection("BUY");
        trade.setLots(BigDecimal.valueOf(0.1));
        trade.setEntryPrice(BigDecimal.valueOf(2300.10));
        trade.setExitPrice(BigDecimal.valueOf(2310.10));
        trade.setPnl(pnl);
        trade.setExitReason("tp");
        trade.setOpenedAt(openedAt);
        trade.setClosedAt(openedAt.plusMinutes(10));
        trade.setRunId("run-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        trade.setAccount(account);
        trade.setCreatedAt(openedAt.plusMinutes(11));
        tradeRepository.save(trade);
    }
}
