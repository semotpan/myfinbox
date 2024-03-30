CREATE TABLE IF NOT EXISTS incomes
(
    id                 UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    account_id         UUID           NOT NULL,
    creation_timestamp TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_type       VARCHAR(20)    NOT NULL,
    amount             DECIMAL(19, 4) NOT NULL,
    currency           VARCHAR(3)     NOT NULL,
    income_date        DATE           NOT NULL,
    description        TEXT,
    income_source_id   UUID           NOT NULL,
    FOREIGN KEY (income_source_id) REFERENCES incomesource (id)
);
