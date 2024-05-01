CREATE TABLE IF NOT EXISTS spending_jars
(
    id                 UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    creation_timestamp TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    name               VARCHAR(255)   NOT NULL,
    amount_to_reach    DECIMAL(19, 4) NOT NULL,
    currency           VARCHAR(3)     NOT NULL,
    percentage         INTEGER        NOT NULL,
    description        TEXT,
    plan_id            UUID           NOT NULL,
    FOREIGN KEY (plan_id) REFERENCES spending_plans (id)
)
;

CREATE UNIQUE INDEX IF NOT EXISTS unique_spending_jar_name_plan_id_idx ON spending_jars (name, plan_id);

CREATE INDEX IF NOT EXISTS search_spending_plan_id_idx ON spending_jars (plan_id);
