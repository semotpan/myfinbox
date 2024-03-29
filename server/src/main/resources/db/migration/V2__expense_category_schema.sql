CREATE TABLE IF NOT EXISTS expensecategory
(
    id                 UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    account_id         UUID         NOT NULL,
    name               VARCHAR(100) NOT NULL,
    creation_timestamp TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX expense_category_account_id_idx ON expensecategory (account_id);
CREATE UNIQUE INDEX unique_category_name_account_id_idx ON expensecategory (account_id, name);
