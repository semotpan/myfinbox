package io.myfinbox.expense.application;

import io.myfinbox.expense.domain.Expenses;
import io.myfinbox.shared.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static io.myfinbox.expense.domain.Expense.ExpenseIdentifier;
import static java.util.Objects.isNull;

@Service
@Transactional
@RequiredArgsConstructor
class DeleteExpenseService implements DeleteExpenseUseCase {

    static final String EXPENSE_NOT_FOUND_MESSAGE = "Expense was not found.";

    private final Expenses expenses;

    @Override
    public Either<Failure, Void> delete(UUID expenseId) {
        if (isNull(expenseId)) {
            return Either.left(Failure.ofNotFound(EXPENSE_NOT_FOUND_MESSAGE));
        }

        var expense = expenses.findById(new ExpenseIdentifier(expenseId));
        if (expense.isEmpty()) {
            return Either.left(Failure.ofNotFound(EXPENSE_NOT_FOUND_MESSAGE));
        }

        expense.get().delete();

        expenses.delete(expense.get());

        return Either.right(null);
    }
}
