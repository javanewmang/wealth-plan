package com.wealthplan.backend.service;

import com.wealthplan.backend.controller.ResourceNotFoundException;
import com.wealthplan.backend.model.AlertEvent;
import com.wealthplan.backend.model.CreateWatchRuleRequest;
import com.wealthplan.backend.model.MarketTick;
import com.wealthplan.backend.model.UpdateWatchRuleRequest;
import com.wealthplan.backend.model.WatchRule;
import com.wealthplan.backend.repository.WatchRuleRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WatchRuleService {
    private final WatchRuleRepository watchRuleRepository;
    private final AlertHistoryService alertHistoryService;
    private final SimpMessagingTemplate messagingTemplate;

    public WatchRuleService(WatchRuleRepository watchRuleRepository,
                            AlertHistoryService alertHistoryService,
                            SimpMessagingTemplate messagingTemplate) {
        this.watchRuleRepository = watchRuleRepository;
        this.alertHistoryService = alertHistoryService;
        this.messagingTemplate = messagingTemplate;
    }

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
        watchRuleRepository.save(rule);
        return rule;
    }

    public WatchRule updateRule(UUID id, UpdateWatchRuleRequest request) {
        WatchRule existing = watchRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("rule not found: " + id));

        WatchRule updated = new WatchRule(
                existing.id(),
                existing.symbol(),
                request.triggerType(),
                request.threshold(),
                request.notifyChannel(),
                request.coolDownSeconds(),
                existing.createdAt(),
                existing.lastTriggeredAt()
        );
        watchRuleRepository.update(updated);
        return updated;
    }

    public List<WatchRule> listRules() {
        return watchRuleRepository.findAllOrderByCreatedAtDesc();
    }

    public boolean deleteRule(UUID id) {
        return watchRuleRepository.deleteById(id);
    }

    public List<AlertEvent> evaluateTick(MarketTick tick) {
        List<AlertEvent> events = new ArrayList<>();
        Instant now = Instant.now();

        for (WatchRule rule : watchRuleRepository.findBySymbol(tick.symbol())) {
            if (!isTriggered(rule, tick) || inCoolDown(rule, now)) {
                continue;
            }

            WatchRule updated = rule.withLastTriggeredAt(now);
            watchRuleRepository.update(updated);
            events.add(new AlertEvent(
                    updated.id(),
                    updated.symbol(),
                    tick.price(),
                    "触发盯盘提醒: " + updated.symbol() + " 当前价格 " + tick.price() +
                            " 命中规则 " + updated.triggerType() + " " + updated.threshold(),
                    now
            ));
        }

        if (!events.isEmpty()) {
            alertHistoryService.appendAll(events);
            messagingTemplate.convertAndSend("/topic/alerts", events);
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
