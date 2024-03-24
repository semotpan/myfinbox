CREATE TABLE IF NOT EXISTS expenses
(
    id                 UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    account_id         UUID           NOT NULL,
    creation_timestamp TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_type       VARCHAR(20)    NOT NULL,
    amount             DECIMAL(19, 4) NOT NULL,
    currency           VARCHAR(3)     NOT NULL,
    expense_date       DATE           NOT NULL,
    description        TEXT,
    category_id        UUID           NOT NULL,
    FOREIGN KEY (category_id) REFERENCES expensecategory (id)
);
