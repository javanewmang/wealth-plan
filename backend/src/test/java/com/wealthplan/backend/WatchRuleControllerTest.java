package com.wealthplan.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WatchRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateAndTriggerWatchRule() throws Exception {
        mockMvc.perform(post("/api/watch-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symbol": "600519",
                                  "triggerType": "PRICE_BELOW",
                                  "threshold": 1600,
                                  "notifyChannel": "APP_PUSH",
                                  "coolDownSeconds": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("600519"));

        mockMvc.perform(post("/api/watch-rules/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symbol": "600519",
                                  "price": 1599
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("600519"));

        mockMvc.perform(get("/api/watch-rules/alerts").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("600519"));
    }

    @Test
    void shouldValidateSymbolAndReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/watch-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symbol": "ABC",
                                  "triggerType": "PRICE_BELOW",
                                  "threshold": 100,
                                  "notifyChannel": "APP_PUSH",
                                  "coolDownSeconds": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }


    @Test
    void shouldUpdateRule() throws Exception {
        String body = mockMvc.perform(post("/api/watch-rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "symbol": "000001",
                                  "triggerType": "PRICE_ABOVE",
                                  "threshold": 10,
                                  "notifyChannel": "APP_PUSH",
                                  "coolDownSeconds": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(put("/api/watch-rules/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "triggerType": "PRICE_BELOW",
                                  "threshold": 9.5,
                                  "notifyChannel": "EMAIL",
                                  "coolDownSeconds": 120
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.triggerType").value("PRICE_BELOW"))
                .andExpect(jsonPath("$.notifyChannel").value("EMAIL"));
    }

    @Test
    void shouldReturnNotFoundWhenDeleteMissingRule() throws Exception {
        mockMvc.perform(delete("/api/watch-rules/123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
