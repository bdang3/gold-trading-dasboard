package com.goldtrading.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.goldtrading.backend.common.RoleType;
import com.goldtrading.backend.users.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResetPasswordFlowIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void resetPasswordFlowWorksEndToEndAndTokenSingleUse() throws Exception {
        User user = createUser(RoleType.USER, "reset-user");

        var forgot = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + user.getEmail() + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode forgotJson = json(forgot.getResponse().getContentAsString());
        assertThat(forgotJson.get("success").asBoolean()).isTrue();
        String token = forgotJson.path("data").path("demoResetToken").asText();
        assertThat(token).isNotBlank();

        var reset = mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"newPassword\":\"NewPass@123\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode resetJson = json(reset.getResponse().getContentAsString());
        assertThat(resetJson.get("success").asBoolean()).isTrue();

        var reuse = mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"newPassword\":\"Another@123\"}"))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode reuseJson = json(reuse.getResponse().getContentAsString());
        assertThat(reuseJson.get("success").asBoolean()).isFalse();
        assertThat(reuseJson.get("code").asText()).isEqualTo("RESET_TOKEN_INVALID");
    }
}
