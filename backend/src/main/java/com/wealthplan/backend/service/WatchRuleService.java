package com.wealthplan.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthplan.backend.model.AlertEvent;
import com.wealthplan.backend.model.CreateWatchRuleRequest;
import com.wealthplan.backend.model.MarketTick;
import com.wealthplan.backend.model.WatchRule;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WatchRuleService {
    private final ConcurrentHashMap<UUID, WatchRule> rules = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final Path storagePath;

    public WatchRuleService(ObjectMapper objectMapper,
                            @Value("${app.rules.storage.path:./data/watch-rules.json}") String storagePath) {
        this.objectMapper = objectMapper;
        this.storagePath = Path.of(storagePath);
    }

    @PostConstruct
    public void loadRules() {
        try {
            if (!Files.exists(storagePath)) {
                ensureStorageDir();
                return;
            }
            List<WatchRule> storedRules = objectMapper.readValue(
                    storagePath.toFile(),
                    new TypeReference<>() {
                    }
            );
            storedRules.forEach(rule -> rules.put(rule.id(), rule));
        } catch (IOException ex) {
            throw new IllegalStateException("failed to load watch rules from " + storagePath, ex);
        }
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
        rules.put(rule.id(), rule);
        persistRules();
        return rule;
    }

    public List<WatchRule> listRules() {
        return rules.values().stream().sorted((a, b) -> b.createdAt().compareTo(a.createdAt())).toList();
    }

    public boolean deleteRule(UUID id) {
        boolean removed = rules.remove(id) != null;
        if (removed) {
            persistRules();
        }
        return removed;
    }

    public List<AlertEvent> evaluateTick(MarketTick tick) {
        List<AlertEvent> events = new ArrayList<>();
        Instant now = Instant.now();
        boolean changed = false;

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
            changed = true;
            events.add(new AlertEvent(
                    updated.id(),
                    updated.symbol(),
                    tick.price(),
                    "触发盯盘提醒: " + updated.symbol() + " 当前价格 " + tick.price() +
                            " 命中规则 " + updated.triggerType() + " " + updated.threshold(),
                    now
            ));
        }
        if (changed) {
            persistRules();
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

    private void ensureStorageDir() throws IOException {
        Path parent = storagePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private void persistRules() {
        try {
            ensureStorageDir();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(storagePath.toFile(), listRules());
        } catch (IOException ex) {
            throw new IllegalStateException("failed to persist watch rules to " + storagePath, ex);
        }
    }
}
