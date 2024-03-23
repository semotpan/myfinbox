package io.myfinbox.expense;

import io.myfinbox.shared.Failure;
import io.vavr.control.Either;

/**
 * Interface for creating expenses.
 */
public interface CreateExpenseUseCase {

    /**
     * Creates an expense based on the provided command.
     *
     * @param command The command containing expense creation details.
     * @return Either a {@link Failure} instance if the expense creation fails, or the created {@link Expense} instance.
     */
    Either<Failure, Expense> create(ExpenseCommand command);
}
