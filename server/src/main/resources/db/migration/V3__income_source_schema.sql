CREATE TABLE IF NOT EXISTS incomesource
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    account_id         UUID         NOT NULL,
    name               VARCHAR(100) NOT NULL,
    creation_timestamp TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX unique_source_name_account_id_idx ON incomesource (account_id, name);
CREATE UNIQUE INDEX unique_income_source_name_account_id_idx ON incomesource (account_id, name);
