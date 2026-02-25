package com.wealthplan.backend.service;

import com.wealthplan.backend.model.AlertEvent;
import com.wealthplan.backend.repository.AlertEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertHistoryService {
    private final AlertEventRepository alertEventRepository;

    public AlertHistoryService(AlertEventRepository alertEventRepository) {
        this.alertEventRepository = alertEventRepository;
    }

    public List<AlertEvent> listRecent(int limit) {
        return alertEventRepository.findRecent(limit);
    }

    public void appendAll(List<AlertEvent> newEvents) {
        if (newEvents == null || newEvents.isEmpty()) {
            return;
        }
        alertEventRepository.saveAll(newEvents);
    }
}
