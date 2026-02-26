package com.wealthplan.backend;

import com.wealthplan.backend.model.AgentCommandResponse;
import com.wealthplan.backend.model.WatchRule;
import com.wealthplan.backend.service.OpenClawCommandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class OpenClawCommandServiceTest {

    @Autowired
    private OpenClawCommandService openClawCommandService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldParseStockNameChannelAndCoolDown() {
        jdbcTemplate.update("DELETE FROM alert_events");
        jdbcTemplate.update("DELETE FROM watch_rules");

        AgentCommandResponse response = openClawCommandService.parseAndCreateRule(
                "帮我盯住贵州茅台，跌破1600用邮件提醒，10分钟内不要提醒第二次"
        );

        assertEquals("CREATE_WATCH_RULE", response.parsedIntent());
        assertNotNull(response.createdRule());
        assertEquals("600519", response.createdRule().symbol());
        assertEquals(WatchRule.TriggerType.PRICE_BELOW, response.createdRule().triggerType());
        assertEquals(WatchRule.NotifyChannel.EMAIL, response.createdRule().notifyChannel());
        assertEquals(600, response.createdRule().coolDownSeconds());
    }

    @Test
    void shouldParseAboveInstructionWithNumericSymbol() {
        jdbcTemplate.update("DELETE FROM alert_events");
        jdbcTemplate.update("DELETE FROM watch_rules");

        AgentCommandResponse response = openClawCommandService.parseAndCreateRule(
                "盯住300750，超过220站内信提醒，静默1小时"
        );

        assertEquals("CREATE_WATCH_RULE", response.parsedIntent());
        assertNotNull(response.createdRule());
        assertEquals("300750", response.createdRule().symbol());
        assertEquals(WatchRule.TriggerType.PRICE_ABOVE, response.createdRule().triggerType());
        assertEquals(WatchRule.NotifyChannel.IN_APP, response.createdRule().notifyChannel());
        assertEquals(3600, response.createdRule().coolDownSeconds());
    }
}
