package io.myfinbox.expense.application;

import io.myfinbox.expense.domain.Expense;
import io.myfinbox.shared.Failure;
import io.vavr.control.Either;

import java.util.UUID;

/**
 * Interface for updating expenses.
 */
public interface UpdateExpenseUseCase {

    /**
     * Updates an expense based on the provided command.
     *
     * @param expenseId The ID of the expense to be updated.
     * @param command   The command containing expense updating details.
     * @return Either a Failure instance if the expense updating fails, or the updated Expense instance.
     */
    Either<Failure, Expense> update(UUID expenseId, ExpenseCommand command);

}
