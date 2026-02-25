package com.wealthplan.backend.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CreateWatchRuleRequest(
        @NotBlank @Pattern(regexp = "\\d{6}", message = "symbol must be a 6-digit A-share code") String symbol,
        @NotNull WatchRule.TriggerType triggerType,
        @NotNull @DecimalMin("0.01") BigDecimal threshold,
        @NotNull WatchRule.NotifyChannel notifyChannel,
        @Min(0) @Max(3600) int coolDownSeconds
) {
}
