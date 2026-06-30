package com.eduplan.voucher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class VoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateThenConnectThenDisconnectFlow() throws Exception {
        String response = mockMvc.perform(post("/api/vouchers/generate")
                        .param("duration", "TWENTY_MIN"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("UNUSED"))
                .andExpect(jsonPath("$.duration").value("20 Minutes"))
                .andReturn().getResponse().getContentAsString();

        String code = objectMapper.readTree(response).get("code").asText();

        mockMvc.perform(post("/api/vouchers/" + code + "/connect")
                        .contentType("application/json")
                        .content("{\"clientIdentifier\":\"laptop-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.clientIdentifier").value("laptop-1"))
                .andExpect(jsonPath("$.secondsRemaining", greaterThanOrEqualTo(0)));

        // a second redeem attempt on a single-use code must be rejected
        mockMvc.perform(post("/api/vouchers/" + code + "/connect")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/vouchers/" + code + "/disconnect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRED"))
                .andExpect(jsonPath("$.forceDisconnected").value(true));
    }

    @Test
    void connectingUnknownCodeReturns404() throws Exception {
        mockMvc.perform(post("/api/vouchers/NOPE-CODE/connect")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void dashboardGroupsBySlot() throws Exception {
        mockMvc.perform(post("/api/vouchers/generate").param("duration", "ONE_HOUR"))
                .andExpect(status().isCreated());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/vouchers/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8));
    }
}
