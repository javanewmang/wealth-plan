CREATE TABLE IF NOT EXISTS watch_rules (
    id UUID PRIMARY KEY,
    symbol VARCHAR(16) NOT NULL,
    trigger_type VARCHAR(32) NOT NULL,
    threshold NUMERIC(18, 4) NOT NULL,
    notify_channel VARCHAR(32) NOT NULL,
    cool_down_seconds INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_triggered_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_watch_rules_symbol ON watch_rules(symbol);

CREATE TABLE IF NOT EXISTS alert_events (
    id BIGSERIAL PRIMARY KEY,
    rule_id UUID NOT NULL,
    symbol VARCHAR(16) NOT NULL,
    current_price NUMERIC(18, 4) NOT NULL,
    message TEXT NOT NULL,
    triggered_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_alert_events_triggered_at ON alert_events(triggered_at DESC);
