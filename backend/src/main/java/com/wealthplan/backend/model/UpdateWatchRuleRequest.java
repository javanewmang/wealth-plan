package com.wealthplan.backend.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateWatchRuleRequest(
        @NotNull WatchRule.TriggerType triggerType,
        @NotNull @DecimalMin("0.01") BigDecimal threshold,
        @NotNull WatchRule.NotifyChannel notifyChannel,
        @Min(0) @Max(3600) int coolDownSeconds
) {
}
