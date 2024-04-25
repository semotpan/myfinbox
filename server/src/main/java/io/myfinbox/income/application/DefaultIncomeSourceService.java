package io.myfinbox.income.application;

import io.myfinbox.income.domain.*;
import io.myfinbox.income.domain.IncomeSource.IncomeSourceIdentifier;
import io.myfinbox.shared.Failure;
import io.myfinbox.shared.Failure.FieldViolation;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static io.myfinbox.income.application.IncomeSourceService.IncomeSourceCommand.FIELD_ACCOUNT_ID;
import static io.myfinbox.income.application.IncomeSourceService.IncomeSourceCommand.FIELD_NAME;
import static io.vavr.API.Invalid;
import static io.vavr.API.Valid;
import static java.text.MessageFormat.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
@RequiredArgsConstructor
class DefaultIncomeSourceService implements IncomeSourceService {

    public static final String VALIDATION_CREATE_FAILURE_MESSAGE = "Validation failed for the create income source request.";
    public static final String VALIDATION_UPDATE_FAILURE_MESSAGE = "Validation failed for the update income source request.";
    public static final String SOURCE_NAME_DUPLICATE_MESSAGE = "A income source with the same name already exists.";
    public static final String SOURCE_NOT_FOUND_MESSAGE = "Income source not found.";
    public static final String SOURCE_IN_USE_FAILURE_MESSAGE = "Income source is currently in use.";

    private final IncomeSourceCommandValidator validator = new IncomeSourceCommandValidator();

    private final IncomeSources incomeSources;
    private final Incomes incomes;

    @Override
    @Transactional
    public Either<Failure, List<IncomeSource>> createDefault(AccountIdentifier account) {
        if (isNull(account)) {
            return Either.left(Failure.ofValidation("AccountIdentifier cannot be null", List.of()));
        }

        var values = DefaultIncomeSources.asList().stream()
                .map(is -> new IncomeSource(is, account))
                .toList();

        incomeSources.saveAll(values);
        log.debug("Default Income Sources {} were created", values);

        return Either.right(values);
    }

    @Override
    @Transactional
    public Either<Failure, IncomeSource> create(IncomeSourceCommand command) {
        var validation = validator.validate(command);
        if (validation.isInvalid()) {
            return Either.left(Failure.ofValidation(VALIDATION_CREATE_FAILURE_MESSAGE, validation.getError().toJavaList()));
        }

        if (incomeSources.existsByNameAndAccount(command.name(), new AccountIdentifier(command.accountId()))) {
            return Either.left(Failure.ofConflict(SOURCE_NAME_DUPLICATE_MESSAGE));
        }

        var incomeSource = new IncomeSource(command.name(), new AccountIdentifier(command.accountId()));
        incomeSources.save(incomeSource);

        log.debug("Income Source {} was created", incomeSource.getId());

        return Either.right(incomeSource);
    }

    @Override
    @Transactional
    public Either<Failure, IncomeSource> update(UUID incomeSourceId, IncomeSourceCommand command) {
        var validation = validator.validate(command);
        if (validation.isInvalid()) {
            return Either.left(Failure.ofValidation(VALIDATION_UPDATE_FAILURE_MESSAGE, validation.getError().toJavaList()));
        }

        if (isNull(incomeSourceId)) {
            return Either.left(Failure.ofNotFound(SOURCE_NOT_FOUND_MESSAGE));
        }

        var possibleIncomeSource = incomeSources.findByIdAndAccount(new IncomeSourceIdentifier(incomeSourceId), new AccountIdentifier(command.accountId()));
        if (possibleIncomeSource.isEmpty()) {
            return Either.left(Failure.ofNotFound(SOURCE_NOT_FOUND_MESSAGE));
        }

        if (possibleIncomeSource.get().sameName(command.name())) {
            return Either.right(possibleIncomeSource.get());
        }

        if (incomeSources.existsByNameAndAccount(command.name(), new AccountIdentifier(command.accountId()))) {
            return Either.left(Failure.ofConflict(SOURCE_NAME_DUPLICATE_MESSAGE));
        }

        possibleIncomeSource.get().setName(command.name());
        incomeSources.save(possibleIncomeSource.get()); // FIXME: fix the save anti-pattern
        log.debug("Income Source {} was updated", possibleIncomeSource.get().getId());

        return Either.right(possibleIncomeSource.get());
    }

    @Override
    @Transactional
    public Either<Failure, Void> delete(UUID incomeSourceId) {
        if (isNull(incomeSourceId)) {
            return Either.left(Failure.ofNotFound(SOURCE_NOT_FOUND_MESSAGE));
        }

        var possibleIncomeSource = incomeSources.findById(new IncomeSourceIdentifier(incomeSourceId));
        if (possibleIncomeSource.isEmpty()) {
            return Either.left(Failure.ofNotFound(SOURCE_NOT_FOUND_MESSAGE));
        }

        if (incomes.existsByIncomeSource(possibleIncomeSource.get())) {
            return Either.left(Failure.ofConflict(SOURCE_IN_USE_FAILURE_MESSAGE));
        }

        incomeSources.delete(possibleIncomeSource.get());
        log.debug("Income Source {} was deleted", possibleIncomeSource.get().getId());

        return Either.right(null);
    }

    private static final class IncomeSourceCommandValidator {

        Validation<Seq<FieldViolation>, IncomeSourceCommand> validate(IncomeSourceCommand command) {
            return Validation.combine(
                    validateAccountId(command.accountId()),
                    validateName(command.name())
            ).ap((accountId, categoryName) -> command);
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

        private Validation<FieldViolation, String> validateName(String name) {
            if (!isBlank(name) && name.length() <= IncomeSource.NAME_MAX_LENGTH) {
                return Valid(name);
            }

            var message = format("Name length cannot exceed {0} characters.", IncomeSource.NAME_MAX_LENGTH);
            if (isBlank(name))
                message = "Name cannot be empty.";

            return Invalid(FieldViolation.builder()
                    .field(FIELD_NAME)
                    .message(message)
                    .rejectedValue(name)
                    .build());
        }
    }
}
