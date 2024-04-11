package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.Failure.FieldViolation;
import io.myfinbox.spendingplan.domain.Plan;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static io.myfinbox.spendingplan.application.PlanCommand.*;
import static io.vavr.API.Invalid;
import static io.vavr.API.Valid;
import static java.math.BigDecimal.ZERO;
import static java.text.MessageFormat.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

final class PlanCommandValidator {

    Validation<Seq<FieldViolation>, PlanCommand> validate(PlanCommand command) {
        return Validation.combine(
                validateName(command.name()),
                validateAccountId(command.accountId()),
                validateAmount(command.amount()),
                validateCurrencyCode(command.currencyCode())
        ).ap((name, account, amount, currencyCode) -> command);
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

    private Validation<FieldViolation, String> validateName(String name) {
        if (!isBlank(name) && name.length() <= Plan.MAX_NAME_LENGTH) {
            return Valid(name);
        }

        var message = format("Name length cannot exceed {0} characters.", Plan.MAX_NAME_LENGTH);
        if (isBlank(name))
            message = "Name cannot be empty.";

        return Invalid(FieldViolation.builder()
                .field(FIELD_NAME)
                .message(message)
                .rejectedValue(name)
                .build());
    }
}
