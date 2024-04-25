package io.myfinbox.expense.application;

import io.myfinbox.expense.domain.Expenses;
import io.myfinbox.shared.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static io.myfinbox.expense.domain.Expense.ExpenseIdentifier;
import static java.util.Objects.isNull;

@Slf4j
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

        var possibleExpense = expenses.findById(new ExpenseIdentifier(expenseId));
        if (possibleExpense.isEmpty()) {
            return Either.left(Failure.ofNotFound(EXPENSE_NOT_FOUND_MESSAGE));
        }

        possibleExpense.get().delete();

        expenses.delete(possibleExpense.get());

        log.debug("Expense {} was deleted", possibleExpense.get().getId());

        return Either.right(null);
    }
}
