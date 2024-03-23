package io.myfinbox.expense.application;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record ExpenseCommand(UUID accountId,
                             UUID categoryId,
                             String paymentType,
                             BigDecimal amount,
                             String currencyCode,
                             LocalDate expenseDate,
                             String description) {

    public static final String FIELD_ACCOUNT_ID = "accountId";
    public static final String FIELD_CATEGORY_ID = "categoryId";
    public static final String FIELD_PAYMENT_TYPE = "paymentType";
    public static final String FIELD_AMOUNT = "amount";
    public static final String FIELD_CURRENCY_CODE = "currencyCode";
    public static final String FIELD_EXPENSE_DATE = "expenseDate";
}
