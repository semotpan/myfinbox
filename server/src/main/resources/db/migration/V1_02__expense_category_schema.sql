CREATE TABLE IF NOT EXISTS expense_category
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    account_id         UUID         NOT NULL,
    name               VARCHAR(100) NOT NULL,
    creation_timestamp TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS expense_category_account_id_idx ON expense_category (account_id);

CREATE UNIQUE INDEX IF NOT EXISTS unique_category_name_account_id_idx ON expense_category (account_id, name);
