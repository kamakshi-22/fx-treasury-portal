-- Trades core transactional table
CREATE TABLE trades (
                        id UUID PRIMARY KEY,
                        trade_ref VARCHAR(64) NOT NULL UNIQUE,
                        currency_pair VARCHAR(16) NOT NULL,
                        side VARCHAR(8) NOT NULL,
                        trade_type VARCHAR(16) NOT NULL,
                        notional NUMERIC(19, 4) NOT NULL,
                        contract_rate NUMERIC(19, 6) NOT NULL,
                        status VARCHAR(16) NOT NULL,
                        trade_date TIMESTAMP WITH TIME ZONE NOT NULL,
                        value_date TIMESTAMP WITH TIME ZONE NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Transactional Outbox table
CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY,
                               aggregate_type VARCHAR(64) NOT NULL,
                               aggregate_id VARCHAR(64) NOT NULL,
                               event_type VARCHAR(64) NOT NULL,
                               payload TEXT NOT NULL,
                               status VARCHAR(16) NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_outbox_status_created ON outbox_events(status, created_at);