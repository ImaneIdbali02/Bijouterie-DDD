-- Migration V2: Création de la table outbox_events
-- Date: 2024-01-01
-- Description: Table pour la gestion des événements en mode outbox pattern

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    topic VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_error TEXT,
    retry_count INTEGER DEFAULT 0,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    processed_at TIMESTAMP
);

-- Index pour outbox_events
CREATE INDEX idx_outbox_events_status ON outbox_events(status);
CREATE INDEX idx_outbox_events_aggregate_id ON outbox_events(aggregate_id);
CREATE INDEX idx_outbox_events_aggregate_type ON outbox_events(aggregate_type);
CREATE INDEX idx_outbox_events_created_at ON outbox_events(created_at);
CREATE INDEX idx_outbox_events_topic ON outbox_events(topic);
CREATE INDEX idx_outbox_events_retry_count ON outbox_events(retry_count);

-- Contrainte pour le statut
ALTER TABLE outbox_events 
ADD CONSTRAINT chk_outbox_events_status 
CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED')); 