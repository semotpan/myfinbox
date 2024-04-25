CREATE TABLE IF NOT EXISTS jar_expense_record
(
    id                      BIGSERIAL PRIMARY KEY ,
    expense_id              UUID           NOT NULL,
    creation_timestamp      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    category_id             UUID           NOT NULL,
    payment_type            VARCHAR(20)    NOT NULL,
    amount                  DECIMAL(19, 4) NOT NULL,
    currency                VARCHAR(3)     NOT NULL,
    expense_date            DATE           NOT NULL,
    jar_expense_category_id BIGINT         NOT NULL,
    FOREIGN KEY (jar_expense_category_id) REFERENCES spending_jar_expense_category (id)
)
;

CREATE INDEX search_jar_expense_category_jar_id_idx ON jar_expense_record (category_id, expense_date, jar_expense_category_id);
