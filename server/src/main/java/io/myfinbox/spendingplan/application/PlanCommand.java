package io.myfinbox.spendingplan.application;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PlanCommand(UUID accountId,
                          BigDecimal amount,
                          String currencyCode,
                          String name,
                          String description) {
    public static final String FIELD_ACCOUNT_ID = "accountId";
    public static final String FIELD_AMOUNT = "amount";
    public static final String FIELD_CURRENCY_CODE = "currencyCode";
    public static final String FIELD_NAME = "name";
}
