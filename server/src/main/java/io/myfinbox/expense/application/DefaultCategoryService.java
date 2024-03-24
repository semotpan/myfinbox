package io.myfinbox.expense.application;

import io.myfinbox.expense.domain.*;
import io.myfinbox.expense.domain.Category.CategoryIdentifier;
import io.myfinbox.shared.Failure;
import io.myfinbox.shared.Failure.FieldViolation;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static io.myfinbox.expense.application.CategoryService.CategoryCommand.FIELD_ACCOUNT_ID;
import static io.myfinbox.expense.application.CategoryService.CategoryCommand.FIELD_NAME;
import static io.vavr.API.Invalid;
import static io.vavr.API.Valid;
import static java.text.MessageFormat.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
class DefaultCategoryService implements CategoryService {

    public static final String VALIDATION_CREATE_FAILURE_MESSAGE = "Validation failed for the create category expense request.";
    public static final String VALIDATION_UPDATE_FAILURE_MESSAGE = "Validation failed for the update category expense request.";
    public static final String CATEGORY_NAME_DUPLICATE_MESSAGE = "A category expense with the same name already exists.";
    public static final String CATEGORY_NOT_FOUND_MESSAGE = "Category expense not found.";
    public static final String CATEGORY_IN_USE_FAILURE_MESSAGE = "Category expense is currently in use.";

    private final CategoryCommandValidator validator = new CategoryCommandValidator();

    private final Categories categories;
    private final Expenses expenses;

    @Override
    @Transactional
    public Either<Failure, List<Category>> createDefault(AccountIdentifier account) {
        if (isNull(account)) {
            return Either.left(Failure.ofValidation("AccountIdentifier cannot be null", List.of()));
        }

        var values = DefaultCategories.asList().stream()
                .map(c -> new Category(c, account))
                .toList();

        categories.saveAll(values);

        return Either.right(values);
    }

    @Override
    @Transactional
    public Either<Failure, Category> create(CategoryCommand command) {
        var validation = validator.validate(command);
        if (validation.isInvalid()) {
            return Either.left(Failure.ofValidation(VALIDATION_CREATE_FAILURE_MESSAGE, validation.getError().toJavaList()));
        }

        if (categories.existsByNameAndAccount(command.name(), new AccountIdentifier(command.accountId()))) {
            return Either.left(Failure.ofConflict(CATEGORY_NAME_DUPLICATE_MESSAGE));
        }

        var category = new Category(command.name(), new AccountIdentifier(command.accountId()));
        categories.save(category);

        return Either.right(category);
    }

    @Override
    @Transactional
    public Either<Failure, Category> update(UUID categoryId, CategoryCommand command) {
        var validation = validator.validate(command);
        if (validation.isInvalid()) {
            return Either.left(Failure.ofValidation(VALIDATION_UPDATE_FAILURE_MESSAGE, validation.getError().toJavaList()));
        }

        if (isNull(categoryId)) {
            return Either.left(Failure.ofNotFound(CATEGORY_NOT_FOUND_MESSAGE));
        }

        var category = categories.findByIdAndAccount(new CategoryIdentifier(categoryId), new AccountIdentifier(command.accountId()));
        if (category.isEmpty()) {
            return Either.left(Failure.ofNotFound(CATEGORY_NOT_FOUND_MESSAGE));
        }

        if (category.get().sameName(command.name())) {
            return Either.right(category.get());
        }

        if (categories.existsByNameAndAccount(command.name(), new AccountIdentifier(command.accountId()))) {
            return Either.left(Failure.ofConflict(CATEGORY_NAME_DUPLICATE_MESSAGE));
        }

        category.get().setName(command.name());
        categories.save(category.get()); // FIXME: fix the save anti-pattern

        return Either.right(category.get());
    }

    @Override
    @Transactional
    public Either<Failure, Void> delete(UUID categoryId) {
        if (isNull(categoryId)) {
            return Either.left(Failure.ofNotFound(CATEGORY_NOT_FOUND_MESSAGE));
        }

        var category = categories.findById(new CategoryIdentifier(categoryId));
        if (category.isEmpty()) {
            return Either.left(Failure.ofNotFound(CATEGORY_NOT_FOUND_MESSAGE));
        }

        if (expenses.existsByCategory(category.get())) {
            return Either.left(Failure.ofConflict(CATEGORY_IN_USE_FAILURE_MESSAGE));
        }

        categories.delete(category.get());

        return Either.right(null);
    }

    private static final class CategoryCommandValidator {

        Validation<Seq<FieldViolation>, CategoryCommand> validate(CategoryCommand command) {
            return Validation.combine(
                    validateAccountId(command.accountId()),
                    validateName(command.name())
            ).ap((accountId, categoryName) -> command);
        }

        private Validation<FieldViolation, UUID> validateAccountId(UUID accountId) {
            if (nonNull(accountId))
                return Valid(accountId);

            return Invalid(FieldViolation.builder()
                    .field(FIELD_ACCOUNT_ID)
                    .message("AccountId cannot be null.")
                    .build());
        }

        private Validation<FieldViolation, String> validateName(String name) {
            if (!isBlank(name) && name.length() <= Category.NAME_MAX_LENGTH)
                return Valid(name);

            var message = format("Name length cannot exceed {0} characters.", Category.NAME_MAX_LENGTH);
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
