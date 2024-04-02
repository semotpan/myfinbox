package io.myfinbox.income.application;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record IncomeCommand(UUID accountId,
                            UUID incomeSourceId,
                            String paymentType,
                            BigDecimal amount,
                            String currencyCode,
                            LocalDate incomeDate,
                            String description) {

    public static final String FIELD_ACCOUNT_ID = "accountId";
    public static final String FIELD_INCOME_SOURCE_ID = "incomeSourceId";
    public static final String FIELD_PAYMENT_TYPE = "paymentType";
    public static final String FIELD_AMOUNT = "amount";
    public static final String FIELD_CURRENCY_CODE = "currencyCode";
    public static final String FIELD_INCOME_DATE = "incomeDate";

}
