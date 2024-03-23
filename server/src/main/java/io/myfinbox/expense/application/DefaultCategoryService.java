package io.myfinbox.expense.application;

import io.myfinbox.expense.domain.AccountIdentifier;
import io.myfinbox.expense.domain.Categories;
import io.myfinbox.expense.domain.Category;
import io.myfinbox.expense.domain.DefaultCategories;
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

    public static final String VALIDATION_CREATE_FAILURE_MESSAGE = "The validation for the create category expense request has failed.";
    public static final String CATEGORY_NAME_DUPLICATE_MESSAGE = "Category name already exists.";

    private final CategoryCommandValidator validator = new CategoryCommandValidator();

    private final Categories categories;

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

            var message = format("Name length cannot be more than {0}.", Category.NAME_MAX_LENGTH);
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
