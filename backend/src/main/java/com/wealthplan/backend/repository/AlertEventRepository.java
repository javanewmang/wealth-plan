package com.wealthplan.backend.repository;

import com.wealthplan.backend.model.AlertEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class AlertEventRepository {
    private final JdbcTemplate jdbcTemplate;

    public AlertEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveAll(List<AlertEvent> events) {
        for (AlertEvent event : events) {
            jdbcTemplate.update("""
                            INSERT INTO alert_events(rule_id, symbol, current_price, message, triggered_at)
                            VALUES (?, ?, ?, ?, ?)
                            """,
                    event.ruleId(),
                    event.symbol(),
                    event.currentPrice(),
                    event.message(),
                    Timestamp.from(event.triggeredAt())
            );
        }
    }

    public List<AlertEvent> findRecent(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        return jdbcTemplate.query("""
                        SELECT rule_id, symbol, current_price, message, triggered_at
                        FROM alert_events
                        ORDER BY triggered_at DESC
                        LIMIT ?
                        """,
                mapper(),
                safeLimit
        );
    }

    private RowMapper<AlertEvent> mapper() {
        return (rs, rowNum) -> new AlertEvent(
                uuid(rs, "rule_id"),
                rs.getString("symbol"),
                rs.getBigDecimal("current_price"),
                rs.getString("message"),
                instant(rs, "triggered_at")
        );
    }

    private UUID uuid(ResultSet rs, String col) throws SQLException {
        Object value = rs.getObject(col);
        if (value instanceof UUID id) {
            return id;
        }
        return UUID.fromString(String.valueOf(value));
    }

    private Instant instant(ResultSet rs, String col) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(col);
        return timestamp == null ? null : timestamp.toInstant();
    }
}
