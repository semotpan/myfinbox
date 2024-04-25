package io.myfinbox.expense.application;

import io.myfinbox.expense.domain.*;
import io.myfinbox.expense.domain.Category.CategoryIdentifier;
import io.myfinbox.expense.domain.Expense.ExpenseIdentifier;
import io.myfinbox.shared.Failure;
import io.myfinbox.shared.PaymentType;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.util.Objects.isNull;

@Slf4j
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

        var possibleExpense = expenses.findByIdAndAccount(new ExpenseIdentifier(expenseId), new AccountIdentifier(command.accountId()));
        if (possibleExpense.isEmpty()) {
            return Either.left(Failure.ofNotFound(EXPENSE_NOT_FOUND_MESSAGE));
        }

        var possibleCategory = fetchCategoryOrFailure(possibleExpense.get().getCategory(), command.categoryId(), command.accountId());
        if (possibleCategory.isLeft()) {
            return Either.left(possibleCategory.getLeft());
        }

        possibleExpense.get().update(
                Expense.builder()
                        .account(new AccountIdentifier(command.accountId()))
                        .amount(Money.of(command.amount(), command.currencyCode()))
                        .expenseDate(command.expenseDate())
                        .paymentType(PaymentType.fromValue(command.paymentType()))
                        .description(command.description())
                        .category(possibleCategory.get())
        );

        expenses.save(possibleExpense.get()); //FIXME: fix save anti-pattern

        log.debug("Expense {} was updated", possibleExpense.get().getId());

        return Either.right(possibleExpense.get());
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
