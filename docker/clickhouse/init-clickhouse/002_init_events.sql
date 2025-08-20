CREATE TABLE IF NOT EXISTS default.Events
(
    `timestamp` DateTime64(3),
    `event_type` LowCardinality(String),
    `entity_id` String,
    `event_id` UUID,
    `data` JSON
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(timestamp)
ORDER BY (timestamp, event_type, entity_id, event_id);