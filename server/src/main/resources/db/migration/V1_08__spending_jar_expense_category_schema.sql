CREATE SEQUENCE IF NOT EXISTS sjec_seq_id START 1 INCREMENT 1;

CREATE TABLE IF NOT EXISTS spending_jar_expense_category
(
    id                 BIGINT PRIMARY KEY DEFAULT nextval('sjec_seq_id'),
    jar_id             UUID      NOT NULL,
    category_id        UUID      NOT NULL,
    category_name      VARCHAR(100),
    creation_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (jar_id) REFERENCES spending_jars (id)
)
;

CREATE UNIQUE INDEX IF NOT EXISTS unique_spending_jar_expense_category_id_idx ON spending_jar_expense_category (jar_id, category_id);

CREATE INDEX IF NOT EXISTS search_expense_category_jar_id_idx ON spending_jar_expense_category (jar_id);
