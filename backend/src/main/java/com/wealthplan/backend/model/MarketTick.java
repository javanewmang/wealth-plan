package com.wealthplan.backend.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record MarketTick(
        @NotBlank @Pattern(regexp = "\\d{6}", message = "symbol must be a 6-digit A-share code") String symbol,
        @NotNull @DecimalMin("0.01") BigDecimal price
) {
}
