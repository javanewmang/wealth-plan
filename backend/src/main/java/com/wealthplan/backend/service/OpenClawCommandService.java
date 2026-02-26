package com.wealthplan.backend.service;

import com.wealthplan.backend.model.AgentCommandResponse;
import com.wealthplan.backend.model.CreateWatchRuleRequest;
import com.wealthplan.backend.model.WatchRule;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OpenClawCommandService {
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("(\\d{6})");
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)");
    private static final Pattern BELOW_PATTERN = Pattern.compile("(跌破|低于)");
    private static final Pattern ABOVE_PATTERN = Pattern.compile("(突破|涨到|高于|超过)");
    private static final Pattern COOL_DOWN_PATTERN = Pattern.compile("(\\d+)\\s*(秒|分钟|小时)(?:内|静默|不要提醒)?");

    private static final int DEFAULT_COOL_DOWN_SECONDS = 300;

    private static final Map<String, String> SYMBOL_ALIAS = Map.of(
            "贵州茅台", "600519",
            "平安银行", "000001",
            "宁德时代", "300750"
    );

    private final WatchRuleService watchRuleService;

    public OpenClawCommandService(WatchRuleService watchRuleService) {
        this.watchRuleService = watchRuleService;
    }

    public AgentCommandResponse parseAndCreateRule(String instruction) {
        String symbol = parseSymbol(instruction);
        BigDecimal threshold = parseThreshold(instruction);
        WatchRule.TriggerType triggerType = parseTriggerType(instruction);

        if (symbol == null || threshold == null || triggerType == null) {
            return new AgentCommandResponse("UNSUPPORTED", null, "暂不支持该指令，请使用“盯住XXXXXX（或股票名），跌破/突破价格提醒我”");
        }

        int coolDownSeconds = parseCoolDownSeconds(instruction);
        WatchRule.NotifyChannel channel = parseNotifyChannel(instruction);

        WatchRule rule = watchRuleService.createRule(new CreateWatchRuleRequest(
                symbol,
                triggerType,
                threshold,
                channel,
                coolDownSeconds
        ));

        String successMessage = triggerType == WatchRule.TriggerType.PRICE_BELOW
                ? "已根据指令创建跌破提醒规则"
                : "已根据指令创建突破提醒规则";
        return new AgentCommandResponse("CREATE_WATCH_RULE", rule, successMessage);
    }

    private String parseSymbol(String instruction) {
        Matcher symbolMatcher = SYMBOL_PATTERN.matcher(instruction);
        if (symbolMatcher.find()) {
            return symbolMatcher.group(1);
        }

        return SYMBOL_ALIAS.entrySet().stream()
                .filter(entry -> instruction.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private BigDecimal parseThreshold(String instruction) {
        Matcher priceMatcher = PRICE_PATTERN.matcher(instruction);
        BigDecimal lastNumber = null;
        while (priceMatcher.find()) {
            lastNumber = new BigDecimal(priceMatcher.group(1));
        }
        return lastNumber;
    }

    private WatchRule.TriggerType parseTriggerType(String instruction) {
        if (BELOW_PATTERN.matcher(instruction).find()) {
            return WatchRule.TriggerType.PRICE_BELOW;
        }
        if (ABOVE_PATTERN.matcher(instruction).find()) {
            return WatchRule.TriggerType.PRICE_ABOVE;
        }
        return null;
    }

    private int parseCoolDownSeconds(String instruction) {
        Matcher coolDownMatcher = COOL_DOWN_PATTERN.matcher(instruction);
        if (!coolDownMatcher.find()) {
            return DEFAULT_COOL_DOWN_SECONDS;
        }

        int value = Integer.parseInt(coolDownMatcher.group(1));
        return switch (coolDownMatcher.group(2)) {
            case "分钟" -> value * 60;
            case "小时" -> value * 3600;
            default -> value;
        };
    }

    private WatchRule.NotifyChannel parseNotifyChannel(String instruction) {
        if (instruction.contains("邮件") || instruction.contains("邮箱") || instruction.contains("email")) {
            return WatchRule.NotifyChannel.EMAIL;
        }
        if (instruction.contains("站内信")) {
            return WatchRule.NotifyChannel.IN_APP;
        }
        return WatchRule.NotifyChannel.APP_PUSH;
    }
}
