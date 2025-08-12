CREATE TABLE IF NOT EXISTS default.Snapshot
(
    date_created DateTime64(3),
    snapshot JSON
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(date_created)
PRIMARY KEY (date_created)
