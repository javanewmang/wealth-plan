package com.wealthplan.backend.service;

import com.wealthplan.backend.model.AgentCommandResponse;
import com.wealthplan.backend.model.CreateWatchRuleRequest;
import com.wealthplan.backend.model.WatchRule;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OpenClawCommandService {
    private static final Pattern BELOW_PATTERN = Pattern.compile("盯住?(\\d{6}).*跌破\\s*(\\d+(?:\\.\\d+)?)");
    private static final Pattern ABOVE_PATTERN = Pattern.compile("盯住?(\\d{6}).*(突破|涨到|高于)\\s*(\\d+(?:\\.\\d+)?)");

    private final WatchRuleService watchRuleService;

    public OpenClawCommandService(WatchRuleService watchRuleService) {
        this.watchRuleService = watchRuleService;
    }

    public AgentCommandResponse parseAndCreateRule(String instruction) {
        Matcher below = BELOW_PATTERN.matcher(instruction);
        if (below.find()) {
            WatchRule rule = watchRuleService.createRule(new CreateWatchRuleRequest(
                    below.group(1),
                    WatchRule.TriggerType.PRICE_BELOW,
                    new BigDecimal(below.group(2)),
                    "APP_PUSH"
            ));
            return new AgentCommandResponse("CREATE_WATCH_RULE", rule, "已根据指令创建跌破提醒规则");
        }

        Matcher above = ABOVE_PATTERN.matcher(instruction);
        if (above.find()) {
            WatchRule rule = watchRuleService.createRule(new CreateWatchRuleRequest(
                    above.group(1),
                    WatchRule.TriggerType.PRICE_ABOVE,
                    new BigDecimal(above.group(3)),
                    "APP_PUSH"
            ));
            return new AgentCommandResponse("CREATE_WATCH_RULE", rule, "已根据指令创建突破提醒规则");
        }

        return new AgentCommandResponse("UNSUPPORTED", null, "暂不支持该指令，请使用“盯住XXXXXX，跌破/突破价格提醒我”");
    }
}
