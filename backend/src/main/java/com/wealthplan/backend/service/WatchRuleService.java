package com.wealthplan.backend.service;

import com.wealthplan.backend.model.AlertEvent;
import com.wealthplan.backend.model.CreateWatchRuleRequest;
import com.wealthplan.backend.model.MarketTick;
import com.wealthplan.backend.model.WatchRule;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WatchRuleService {
    private final ConcurrentHashMap<UUID, WatchRule> rules = new ConcurrentHashMap<>();

    public WatchRule createRule(CreateWatchRuleRequest request) {
        WatchRule rule = new WatchRule(
                UUID.randomUUID(),
                request.symbol().toUpperCase(),
                request.triggerType(),
                request.threshold(),
                request.notifyChannel(),
                request.coolDownSeconds(),
                Instant.now(),
                null
        );
        rules.put(rule.id(), rule);
        return rule;
    }

    public List<WatchRule> listRules() {
        return rules.values().stream().sorted((a, b) -> b.createdAt().compareTo(a.createdAt())).toList();
    }

    public boolean deleteRule(UUID id) {
        return rules.remove(id) != null;
    }

    public List<AlertEvent> evaluateTick(MarketTick tick) {
        List<AlertEvent> events = new ArrayList<>();
        Instant now = Instant.now();

        for (WatchRule rule : rules.values()) {
            if (!rule.symbol().equalsIgnoreCase(tick.symbol())) {
                continue;
            }
            if (!isTriggered(rule, tick)) {
                continue;
            }
            if (inCoolDown(rule, now)) {
                continue;
            }

            WatchRule updated = rule.withLastTriggeredAt(now);
            rules.put(updated.id(), updated);
            events.add(new AlertEvent(
                    updated.id(),
                    updated.symbol(),
                    tick.price(),
                    "触发盯盘提醒: " + updated.symbol() + " 当前价格 " + tick.price() +
                            " 命中规则 " + updated.triggerType() + " " + updated.threshold(),
                    now
            ));
        }
        return events;
    }

    private boolean isTriggered(WatchRule rule, MarketTick tick) {
        return switch (rule.triggerType()) {
            case PRICE_ABOVE -> tick.price().compareTo(rule.threshold()) >= 0;
            case PRICE_BELOW -> tick.price().compareTo(rule.threshold()) <= 0;
        };
    }

    private boolean inCoolDown(WatchRule rule, Instant now) {
        if (rule.lastTriggeredAt() == null || rule.coolDownSeconds() == 0) {
            return false;
        }
        return Duration.between(rule.lastTriggeredAt(), now).getSeconds() < rule.coolDownSeconds();
    }
}
