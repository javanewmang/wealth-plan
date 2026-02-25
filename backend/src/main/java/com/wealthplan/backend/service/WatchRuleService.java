package com.wealthplan.backend.service;

import com.wealthplan.backend.model.AlertEvent;
import com.wealthplan.backend.model.CreateWatchRuleRequest;
import com.wealthplan.backend.model.MarketTick;
import com.wealthplan.backend.model.WatchRule;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WatchRuleService {
    private final CopyOnWriteArrayList<WatchRule> rules = new CopyOnWriteArrayList<>();

    public WatchRule createRule(CreateWatchRuleRequest request) {
        WatchRule rule = new WatchRule(
                UUID.randomUUID(),
                request.symbol().toUpperCase(),
                request.triggerType(),
                request.threshold(),
                request.notifyChannel(),
                Instant.now()
        );
        rules.add(rule);
        return rule;
    }

    public List<WatchRule> listRules() {
        return List.copyOf(rules);
    }

    public List<AlertEvent> evaluateTick(MarketTick tick) {
        List<AlertEvent> events = new ArrayList<>();
        for (WatchRule rule : rules) {
            if (!rule.symbol().equalsIgnoreCase(tick.symbol())) {
                continue;
            }
            boolean triggered = switch (rule.triggerType()) {
                case PRICE_ABOVE -> tick.price().compareTo(rule.threshold()) >= 0;
                case PRICE_BELOW -> tick.price().compareTo(rule.threshold()) <= 0;
            };
            if (triggered) {
                events.add(new AlertEvent(
                        rule.id(),
                        rule.symbol(),
                        tick.price(),
                        "触发盯盘提醒: " + rule.symbol() + " 当前价格 " + tick.price() +
                                " 命中规则 " + rule.triggerType() + " " + rule.threshold(),
                        Instant.now()
                ));
            }
        }
        return events;
    }
}
