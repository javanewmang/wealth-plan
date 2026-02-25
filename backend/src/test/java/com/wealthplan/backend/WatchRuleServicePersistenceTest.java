package com.wealthplan.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthplan.backend.model.CreateWatchRuleRequest;
import com.wealthplan.backend.model.MarketTick;
import com.wealthplan.backend.model.WatchRule;
import com.wealthplan.backend.service.WatchRuleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WatchRuleServicePersistenceTest {

    @Test
    void shouldPersistAndReloadRules(@TempDir Path tempDir) {
        Path file = tempDir.resolve("watch-rules.json");

        WatchRuleService service1 = new WatchRuleService(new ObjectMapper(), file.toString());
        service1.loadRules();
        service1.createRule(new CreateWatchRuleRequest(
                "600519",
                WatchRule.TriggerType.PRICE_BELOW,
                new BigDecimal("1600"),
                WatchRule.NotifyChannel.APP_PUSH,
                300
        ));

        WatchRuleService service2 = new WatchRuleService(new ObjectMapper(), file.toString());
        service2.loadRules();

        assertEquals(1, service2.listRules().size());
        assertEquals(1, service2.evaluateTick(new MarketTick("600519", new BigDecimal("1590"))).size());
        assertEquals(0, service2.evaluateTick(new MarketTick("600519", new BigDecimal("1590"))).size());
    }
}
