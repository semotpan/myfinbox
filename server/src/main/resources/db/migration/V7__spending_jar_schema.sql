CREATE TABLE IF NOT EXISTS spendingjars
(
    id                 UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    creation_timestamp TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    name               VARCHAR(255)   NOT NULL,
    amount_to_reach    DECIMAL(19, 4) NOT NULL,
    currency           VARCHAR(3)     NOT NULL,
    percentage         INTEGER        NOT NULL,
    description        TEXT,
    plan_id            UUID           NOT NULL,
    FOREIGN KEY (plan_id) REFERENCES spendingplans (id)
)
;

CREATE UNIQUE INDEX unique_spending_jar_name_plan_id_idx ON spendingjars (name, plan_id);

CREATE INDEX search_spending_plan_id_idx ON spendingjars (plan_id);
