package com.wealthplan.backend.model;

import jakarta.validation.constraints.NotBlank;

public record AgentCommandRequest(@NotBlank String instruction) {
}
