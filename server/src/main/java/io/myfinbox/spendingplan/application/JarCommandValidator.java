package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.Failure.FieldViolation;
import io.myfinbox.spendingplan.domain.Jar;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import static io.myfinbox.spendingplan.application.JarCommand.FIELD_NAME;
import static io.myfinbox.spendingplan.application.JarCommand.FIELD_PERCENTAGE;
import static io.vavr.API.Invalid;
import static io.vavr.API.Valid;
import static java.text.MessageFormat.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

final class JarCommandValidator {

    Validation<Seq<FieldViolation>, JarCommand> validate(JarCommand command) {
        return Validation.combine(
                validateName(command.name()),
                validatePercentage(command.percentage())
        ).ap((name, percentage) -> command);
    }

    private Validation<FieldViolation, String> validateName(String name) {
        if (!isBlank(name) && name.length() <= Jar.MAX_NAME_LENGTH) {
            return Valid(name);
        }

        var message = format("Name length cannot exceed {0} characters.", Jar.MAX_NAME_LENGTH);
        if (isBlank(name)) {
            message = "Name cannot be empty.";
        }

        return Invalid(FieldViolation.builder()
                .field(FIELD_NAME)
                .message(message)
                .rejectedValue(name)
                .build());
    }

    private Validation<FieldViolation, Integer> validatePercentage(Integer percentage) {
        if (nonNull(percentage) && percentage > 0 && percentage <= 100) {
            return Valid(percentage);
        }

        var message = "Percentage must be between 1 and 100.";
        if (isNull(percentage)) {
            message = "Percentage cannot be null.";
        }

        return Invalid(FieldViolation.builder()
                .field(FIELD_PERCENTAGE)
                .message(message)
                .rejectedValue(percentage)
                .build());
    }
}
