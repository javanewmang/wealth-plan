package com.wealthplan.backend.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MarketTick(
        @NotBlank String symbol,
        @NotNull @DecimalMin("0.01") BigDecimal price
) {
}
