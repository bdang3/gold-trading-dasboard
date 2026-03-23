package com.goldtrading.backend.integration;

import com.goldtrading.backend.common.*;
import com.goldtrading.backend.mt5accounts.domain.entity.MT5Account;
import com.goldtrading.backend.users.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminContractsIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void dashboardSummaryReturnsTypedStableStructure() throws Exception {
        User admin = createUser(RoleType.ADMIN, "admin-dashboard");
        User owner = createUser(RoleType.USER, "owner-dashboard");
        createAccount(owner, AccountStatus.PENDING, VerificationStatus.UNVERIFIED, AdminActionState.PENDING_ADMIN);
        createPort(PortStatus.AVAILABLE);
        createPort(PortStatus.DISABLED);

        var res = mockMvc.perform(get("/api/v1/admin/dashboard/summary")
                        .header(authHeaderName(), bearer(admin)))
                .andExpect(status().isOk())
                .andReturn();

        var body = json(res.getResponse().getContentAsString());
        assertThat(body.path("success").asBoolean()).isTrue();
        var data = body.path("data");
        assertThat(data.has("totalUsers")).isTrue();
        assertThat(data.has("totalMt5Accounts")).isTrue();
        assertThat(data.has("pendingAccounts")).isTrue();
        assertThat(data.has("processingAccounts")).isTrue();
        assertThat(data.has("stoppedAccounts")).isTrue();
        assertThat(data.has("failedAccounts")).isTrue();
        assertThat(data.has("availablePorts")).isTrue();
        assertThat(data.has("occupiedPorts")).isTrue();
        assertThat(data.has("disabledPorts")).isTrue();
        assertThat(data.has("recentAlertsCount")).isTrue();
    }

    @Test
    void adminMt5AccountsListSupportsPagingFilterSortAndSearch() throws Exception {
        User admin = createUser(RoleType.ADMIN, "admin-list");
        User matchedOwner = createUser(RoleType.USER, "john-target");
        User otherOwner = createUser(RoleType.USER, "amy-other");

        MT5Account a1 = createAccount(matchedOwner, AccountStatus.PENDING, VerificationStatus.VERIFIED, AdminActionState.PENDING_ADMIN);
        a1.setAccountNumber("MT5-001-A");
        a1.setBroker("Exness");
        a1.setTimeframe("M5");
        mt5AccountRepository.save(a1);

        MT5Account a2 = createAccount(otherOwner, AccountStatus.PROCESSING, VerificationStatus.FAILED, AdminActionState.PROCESSING);
        a2.setAccountNumber("MT5-999-Z");
        a2.setBroker("ICMarkets");
        a2.setTimeframe("H1");
        mt5AccountRepository.save(a2);

        var pageRes = mockMvc.perform(get("/api/v1/admin/mt5-accounts")
                        .header(authHeaderName(), bearer(admin))
                        .param("page", "0")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andReturn();
        var pageJson = json(pageRes.getResponse().getContentAsString()).path("data");
        assertThat(pageJson.path("items").isArray()).isTrue();
        assertThat(pageJson.path("items").size()).isEqualTo(1);
        assertThat(pageJson.path("page").asInt()).isEqualTo(0);
        assertThat(pageJson.path("pageSize").asInt()).isEqualTo(1);
        assertThat(pageJson.path("totalItems").asLong()).isGreaterThanOrEqualTo(2L);

        var filterRes = mockMvc.perform(get("/api/v1/admin/mt5-accounts")
                        .header(authHeaderName(), bearer(admin))
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andReturn();
        var filterItems = json(filterRes.getResponse().getContentAsString()).path("data").path("items");
        assertThat(filterItems.size()).isGreaterThanOrEqualTo(1);
        for (var item : filterItems) {
            assertThat(item.path("status").asText()).isEqualTo("PENDING");
        }

        var sortRes = mockMvc.perform(get("/api/v1/admin/mt5-accounts")
                        .header(authHeaderName(), bearer(admin))
                        .param("sortBy", "accountNumber")
                        .param("sortOrder", "asc")
                        .param("page", "0")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andReturn();
        var sortItems = json(sortRes.getResponse().getContentAsString()).path("data").path("items");
        if (sortItems.size() >= 2) {
            String first = sortItems.get(0).path("accountNumber").asText();
            String second = sortItems.get(1).path("accountNumber").asText();
            assertThat(first.compareTo(second)).isLessThanOrEqualTo(0);
        }

        var searchRes = mockMvc.perform(get("/api/v1/admin/mt5-accounts")
                        .header(authHeaderName(), bearer(admin))
                        .param("search", "john-target")
                        .param("page", "0")
                        .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andReturn();
        var searchItems = json(searchRes.getResponse().getContentAsString()).path("data").path("items");
        assertThat(searchItems.size()).isGreaterThanOrEqualTo(1);
        boolean containsTarget = false;
        for (var item : searchItems) {
            if (item.path("userId").asText().equals(matchedOwner.getId().toString())) {
                containsTarget = true;
            }
        }
        assertThat(containsTarget).isTrue();
    }
}
