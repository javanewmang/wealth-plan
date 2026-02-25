package com.wealthplan.backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WatchRule(
        UUID id,
        String symbol,
        TriggerType triggerType,
        BigDecimal threshold,
        NotifyChannel notifyChannel,
        int coolDownSeconds,
        Instant createdAt,
        Instant lastTriggeredAt
) {
    public enum TriggerType {
        PRICE_ABOVE,
        PRICE_BELOW
    }

    public enum NotifyChannel {
        APP_PUSH,
        EMAIL,
        IN_APP
    }

    public WatchRule withLastTriggeredAt(Instant lastTriggeredAt) {
        return new WatchRule(id, symbol, triggerType, threshold, notifyChannel, coolDownSeconds, createdAt, lastTriggeredAt);
    }
}
