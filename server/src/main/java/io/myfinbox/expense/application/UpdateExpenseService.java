package io.myfinbox.expense.application;

import io.myfinbox.expense.domain.*;
import io.myfinbox.expense.domain.Category.CategoryIdentifier;
import io.myfinbox.expense.domain.Expense.ExpenseIdentifier;
import io.myfinbox.shared.Failure;
import io.myfinbox.shared.PaymentType;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Transactional
class UpdateExpenseService implements UpdateExpenseUseCase {

    static final String VALIDATION_FAILURE_MESSAGE = "Validation failed for the update expense request.";
    static final String CATEGORY_NOT_FOUND_MESSAGE = "Category not found for the provided account.";
    static final String EXPENSE_NOT_FOUND_MESSAGE = "Expense not found.";

    private final ExpenseCommandValidator validator = new ExpenseCommandValidator();

    private final Categories categories;
    private final Expenses expenses;

    @Override
    public Either<Failure, Expense> update(UUID expenseId, ExpenseCommand command) {
        var validation = validator.validate(command);
        if (validation.isInvalid()) {
            return Either.left(Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, validation.getError().toJavaList()));
        }

        if (isNull(expenseId)) {
            return Either.left(Failure.ofNotFound(EXPENSE_NOT_FOUND_MESSAGE));
        }

        var expense = expenses.findByIdAndAccount(new ExpenseIdentifier(expenseId), new AccountIdentifier(command.accountId()));
        if (expense.isEmpty()) {
            return Either.left(Failure.ofNotFound(EXPENSE_NOT_FOUND_MESSAGE));
        }

        var possibleCategory = fetchCategoryOrFailure(expense.get().getCategory(), command.categoryId(), command.accountId());
        if (possibleCategory.isLeft()) {
            return Either.left(possibleCategory.getLeft());
        }

        expense.get().update(
                Money.of(command.amount(), command.currencyCode()),
                PaymentType.fromValue(command.paymentType()),
                command.expenseDate(),
                command.description(),
                possibleCategory.get()
        );

        expenses.save(expense.get());

        return Either.right(expense.get());
    }

    private Either<Failure, Category> fetchCategoryOrFailure(Category category, UUID categoryId, UUID accountId) {
        if (!category.matches(new CategoryIdentifier(categoryId))) {
            var possibleCategory = categories.findByIdAndAccount(new CategoryIdentifier(categoryId), new AccountIdentifier(accountId));
            if (possibleCategory.isEmpty()) {
                return Either.left(Failure.ofNotFound(CATEGORY_NOT_FOUND_MESSAGE));
            }
            category = possibleCategory.get();
        }

        return Either.right(category);
    }
}
