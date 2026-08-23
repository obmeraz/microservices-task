CREATE TABLE IF NOT EXISTS resources
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    storage_key
    VARCHAR
(
    512
) NOT NULL,
    storage_type VARCHAR
(
    32
) NOT NULL,
    bucket VARCHAR
(
    100
) NOT NULL,
    path VARCHAR
(
    100
) NOT NULL
    );
