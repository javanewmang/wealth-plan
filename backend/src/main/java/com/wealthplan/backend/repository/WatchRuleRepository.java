package com.wealthplan.backend.repository;

import com.wealthplan.backend.model.WatchRule;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WatchRuleRepository {
    private final JdbcTemplate jdbcTemplate;

    public WatchRuleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(WatchRule rule) {
        jdbcTemplate.update("""
                        INSERT INTO watch_rules(id, symbol, trigger_type, threshold, notify_channel, cool_down_seconds, created_at, last_triggered_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                rule.id(),
                rule.symbol(),
                rule.triggerType().name(),
                rule.threshold(),
                rule.notifyChannel().name(),
                rule.coolDownSeconds(),
                Timestamp.from(rule.createdAt()),
                ts(rule.lastTriggeredAt())
        );
    }

    public void update(WatchRule rule) {
        jdbcTemplate.update("""
                        UPDATE watch_rules
                        SET trigger_type=?, threshold=?, notify_channel=?, cool_down_seconds=?, last_triggered_at=?
                        WHERE id=?
                        """,
                rule.triggerType().name(),
                rule.threshold(),
                rule.notifyChannel().name(),
                rule.coolDownSeconds(),
                ts(rule.lastTriggeredAt()),
                rule.id()
        );
    }

    public Optional<WatchRule> findById(UUID id) {
        List<WatchRule> result = jdbcTemplate.query("SELECT * FROM watch_rules WHERE id = ?", mapper(), id);
        return result.stream().findFirst();
    }

    public List<WatchRule> findAllOrderByCreatedAtDesc() {
        return jdbcTemplate.query("SELECT * FROM watch_rules ORDER BY created_at DESC", mapper());
    }

    public List<WatchRule> findBySymbol(String symbol) {
        return jdbcTemplate.query("SELECT * FROM watch_rules WHERE symbol = ?", mapper(), symbol.toUpperCase());
    }

    public boolean deleteById(UUID id) {
        return jdbcTemplate.update("DELETE FROM watch_rules WHERE id = ?", id) > 0;
    }

    private Timestamp ts(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private RowMapper<WatchRule> mapper() {
        return (rs, rowNum) -> new WatchRule(
                uuid(rs, "id"),
                rs.getString("symbol"),
                WatchRule.TriggerType.valueOf(rs.getString("trigger_type")),
                rs.getBigDecimal("threshold"),
                WatchRule.NotifyChannel.valueOf(rs.getString("notify_channel")),
                rs.getInt("cool_down_seconds"),
                instant(rs, "created_at"),
                instant(rs, "last_triggered_at")
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
