package com.wealthplan.backend;

import com.wealthplan.backend.model.CreateWatchRuleRequest;
import com.wealthplan.backend.model.MarketTick;
import com.wealthplan.backend.model.WatchRule;
import com.wealthplan.backend.service.AlertHistoryService;
import com.wealthplan.backend.service.WatchRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class WatchRuleServicePersistenceTest {

    @Autowired
    private WatchRuleService watchRuleService;

    @Autowired
    private AlertHistoryService alertHistoryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() {
        jdbcTemplate.update("DELETE FROM alert_events");
        jdbcTemplate.update("DELETE FROM watch_rules");
    }

    @Test
    void shouldPersistAndQueryAlerts() {
        watchRuleService.createRule(new CreateWatchRuleRequest(
                "600519",
                WatchRule.TriggerType.PRICE_BELOW,
                new BigDecimal("1600"),
                WatchRule.NotifyChannel.APP_PUSH,
                300
        ));

        assertEquals(1, watchRuleService.listRules().size());
        assertEquals(1, watchRuleService.evaluateTick(new MarketTick("600519", new BigDecimal("1590"))).size());
        assertEquals(0, watchRuleService.evaluateTick(new MarketTick("600519", new BigDecimal("1590"))).size());
        assertEquals(1, alertHistoryService.listRecent(20).size());
    }
}
