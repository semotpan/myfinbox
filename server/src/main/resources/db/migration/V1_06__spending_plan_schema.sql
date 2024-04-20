CREATE TABLE IF NOT EXISTS spending_plans
(
    id                 UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    creation_timestamp TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    account_id         UUID           NOT NULL,
    name               VARCHAR(255)   NOT NULL,
    amount             DECIMAL(19, 4) NOT NULL,
    currency           VARCHAR(3)     NOT NULL,
    description        TEXT
)
;

CREATE UNIQUE INDEX unique_spending_plan_name_account_id_idx ON spending_plans (account_id, name);
