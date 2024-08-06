CREATE TABLE IF NOT EXISTS accounts
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    creation_timestamp TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    email_address      VARCHAR(256) NOT NULL UNIQUE,
    first_name         VARCHAR(255),
    last_name          VARCHAR(255)
)
;
