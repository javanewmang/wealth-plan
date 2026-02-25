package com.wealthplan.backend.controller;

import com.wealthplan.backend.model.AgentCommandRequest;
import com.wealthplan.backend.model.AgentCommandResponse;
import com.wealthplan.backend.service.OpenClawCommandService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final OpenClawCommandService openClawCommandService;

    public AgentController(OpenClawCommandService openClawCommandService) {
        this.openClawCommandService = openClawCommandService;
    }

    @PostMapping("/commands")
    public AgentCommandResponse executeCommand(@Valid @RequestBody AgentCommandRequest request) {
        return openClawCommandService.parseAndCreateRule(request.instruction());
    }
}
