CREATE TABLE IF NOT EXISTS default.Events
(
    date_created DateTime64(3),
    type String,
    /* store it as a json or type all the possbile fields? */
    data JSON
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(ts)
PRIMARY KEY (date_created)