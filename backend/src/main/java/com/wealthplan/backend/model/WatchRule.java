package com.wealthplan.backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WatchRule(
        UUID id,
        String symbol,
        TriggerType triggerType,
        BigDecimal threshold,
        String notifyChannel,
        Instant createdAt
) {
    public enum TriggerType {
        PRICE_ABOVE,
        PRICE_BELOW
    }
}
