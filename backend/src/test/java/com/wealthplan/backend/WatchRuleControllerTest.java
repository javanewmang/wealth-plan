package com.wealthplan.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                                  "notifyChannel": "APP_PUSH"
                                }
                                """))
                .andExpect(status().isOk())
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
    }
}
