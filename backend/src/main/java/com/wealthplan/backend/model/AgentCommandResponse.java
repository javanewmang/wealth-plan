package com.wealthplan.backend.model;

public record AgentCommandResponse(String parsedIntent, WatchRule createdRule, String message) {
}
