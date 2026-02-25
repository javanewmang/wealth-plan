package com.wealthplan.backend.controller;

import com.wealthplan.backend.model.AlertEvent;
import com.wealthplan.backend.model.CreateWatchRuleRequest;
import com.wealthplan.backend.model.MarketTick;
import com.wealthplan.backend.model.WatchRule;
import com.wealthplan.backend.service.WatchRuleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/watch-rules")
public class WatchRuleController {
    private final WatchRuleService watchRuleService;

    public WatchRuleController(WatchRuleService watchRuleService) {
        this.watchRuleService = watchRuleService;
    }

    @PostMapping
    public WatchRule createRule(@Valid @RequestBody CreateWatchRuleRequest request) {
        return watchRuleService.createRule(request);
    }

    @GetMapping
    public List<WatchRule> listRules() {
        return watchRuleService.listRules();
    }

    @PostMapping("/evaluate")
    public List<AlertEvent> evaluate(@Valid @RequestBody MarketTick tick) {
        return watchRuleService.evaluateTick(tick);
    }
}
