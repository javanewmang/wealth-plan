package com.wealthplan.backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AlertEvent(
        UUID ruleId,
        String symbol,
        BigDecimal currentPrice,
        String message,
        Instant triggeredAt
) {
}
