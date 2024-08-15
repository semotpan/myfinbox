package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.Failure;
import io.vavr.control.Validation;

import java.util.List;
import java.util.stream.Collectors;

import static io.myfinbox.shared.Failure.FieldViolation;
import static io.myfinbox.spendingplan.application.AddOrRemoveJarCategoryUseCase.JarCategoriesCommand.CATEGORIES_JAR_FIELD;
import static io.myfinbox.spendingplan.application.AddOrRemoveJarCategoryUseCase.JarCategoryToAddOrRemove;
import static io.vavr.API.Invalid;
import static io.vavr.API.Valid;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

final class CategoriesJarCommandValidator {

    Validation<FieldViolation, List<JarCategoryToAddOrRemove>> validate(List<JarCategoryToAddOrRemove> categories) {
        if (isNull(categories) || categories.isEmpty()) {
            return Invalid(Failure.FieldViolation.builder()
                    .field(CATEGORIES_JAR_FIELD)
                    .message("At least one category must be provided.")
                    .rejectedValue(categories)
                    .build());
        }

        var anyNull = categories.stream()
                .anyMatch(category -> isNull(category.categoryId()) || isBlank(category.categoryName()));

        if (anyNull) {
            return Invalid(Failure.FieldViolation.builder()
                    .field(CATEGORIES_JAR_FIELD)
                    .message("Null categoryId or blank categoryName not allowed.")
                    .rejectedValue(categories)
                    .build());
        }

        var categoryCount = categories.stream()
                .collect(Collectors.groupingBy(JarCategoryToAddOrRemove::categoryId));

        var anyDuplicate = categoryCount.values().stream()
                .map(List::size)
                .anyMatch(size -> size > 1);

        if (anyDuplicate) {
            return Invalid(Failure.FieldViolation.builder()
                    .field(CATEGORIES_JAR_FIELD)
                    .message("Duplicate category ids provided.")
                    .rejectedValue(categories)
                    .build());
        }

        return Valid(categories);
    }
}
