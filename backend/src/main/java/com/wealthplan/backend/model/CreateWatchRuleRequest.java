package com.wealthplan.backend.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateWatchRuleRequest(
        @NotBlank String symbol,
        @NotNull WatchRule.TriggerType triggerType,
        @NotNull @DecimalMin("0.01") BigDecimal threshold,
        @NotBlank String notifyChannel
) {
}
