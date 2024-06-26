package io.myfinbox.income.application;

import io.myfinbox.shared.Failure.FieldViolation;
import io.myfinbox.shared.PaymentType;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

import static io.myfinbox.income.application.IncomeCommand.*;
import static io.vavr.API.Invalid;
import static io.vavr.API.Valid;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

final class IncomeCommandValidator {

    Validation<Seq<FieldViolation>, IncomeCommand> validate(IncomeCommand command) {
        return Validation.combine(
                validateAccountId(command.accountId()),
                validateIncomeSourceId(command.incomeSourceId()),
                validatePaymentType(command.paymentType()),
                validateAmount(command.amount()),
                validateCurrencyCode(command.currencyCode()),
                validateIncomeDate(command.incomeDate())
        ).ap((accountId, incomeSourceId, paymentType, amount, currencyCode, incomeDate) -> command);
    }

    private Validation<FieldViolation, UUID> validateAccountId(UUID accountId) {
        if (nonNull(accountId)) {
            return Valid(accountId);
        }

        return Invalid(FieldViolation.builder()
                .field(FIELD_ACCOUNT_ID)
                .message("AccountId cannot be null.")
                .build());
    }

    private Validation<FieldViolation, UUID> validateIncomeSourceId(UUID incomeSourceId) {
        if (nonNull(incomeSourceId)) {
            return Valid(incomeSourceId);
        }

        return Invalid(FieldViolation.builder()
                .field(FIELD_INCOME_SOURCE_ID)
                .message("IncomeSourceId cannot be null.")
                .build());
    }

    private Validation<FieldViolation, String> validatePaymentType(String paymentType) {
        if (nonNull(paymentType) && PaymentType.containsValue(paymentType)) {
            return Valid(paymentType);
        }

        return Invalid(FieldViolation.builder()
                .field(FIELD_PAYMENT_TYPE)
                .message("PaymentType must be 'Cash' or 'Card'.")
                .rejectedValue(paymentType)
                .build());
    }

    private Validation<FieldViolation, BigDecimal> validateAmount(BigDecimal amount) {
        if (nonNull(amount) && amount.compareTo(ZERO) > 0) {
            return Valid(amount);
        }

        var message = "Amount must be a positive value.";
        if (isNull(amount)) {
            message = "Amount cannot be null.";
        }

        return Invalid(FieldViolation.builder()
                .field(FIELD_AMOUNT)
                .message(message)
                .rejectedValue(amount)
                .build());
    }

    private Validation<FieldViolation, String> validateCurrencyCode(String currencyCode) {
        String message;
        try {
            Currency.getInstance(currencyCode);
            return Valid(currencyCode);
        } catch (NullPointerException ignored) {
            message = "CurrencyCode cannot be null.";
        } catch (IllegalArgumentException ignored) {
            message = "CurrencyCode is not valid.";
        }

        return Invalid(FieldViolation.builder()
                .field(FIELD_CURRENCY_CODE)
                .message(message)
                .rejectedValue(currencyCode)
                .build());
    }

    private Validation<FieldViolation, LocalDate> validateIncomeDate(LocalDate incomeDate) {
        if (nonNull(incomeDate)) {
            return Valid(incomeDate);
        }

        return Invalid(FieldViolation.builder()
                .field(FIELD_INCOME_DATE)
                .message("IncomeDate cannot be null.")
                .build());
    }
}
