CREATE TABLE IF NOT EXISTS spending_jar_expense_category
(
    id                 BIGSERIAL PRIMARY KEY,
    jar_id             UUID      NOT NULL,
    category_id        UUID      NOT NULL,
    creation_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (jar_id) REFERENCES spending_jars (id)
)
;

CREATE UNIQUE INDEX unique_spending_jar_expense_category_id_idx ON spending_jar_expense_category (jar_id, category_id);

CREATE INDEX search_expense_category_jar_id_idx ON spending_jar_expense_category (jar_id);
