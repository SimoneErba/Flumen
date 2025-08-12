CREATE TABLE IF NOT EXISTS default.Events
(
    date_created DateTime64(3),
    type String,
    data JSON
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(date_created)
PRIMARY KEY (date_created)
