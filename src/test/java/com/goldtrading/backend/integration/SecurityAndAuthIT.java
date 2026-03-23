package com.goldtrading.backend.integration;

import com.goldtrading.backend.common.RoleType;
import com.goldtrading.backend.users.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityAndAuthIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void userCannotAccessAdminEndpoints() throws Exception {
        User user = createUser(RoleType.USER, "plain-user");
        mockMvc.perform(get("/api/v1/admin/users").header(authHeaderName(), bearer(user))).andExpect(status().isForbidden());
    }

    @Test
    void privateAuthEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/auth/logout").contentType("application/json").content("{\"refreshToken\":\"x\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/auth/change-password").contentType("application/json").content("{\"currentPassword\":\"a\",\"newPassword\":\"b\"}"))
                .andExpect(status().isForbidden());
    }
}
