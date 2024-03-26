package io.myfinbox.expense.application;

import io.myfinbox.shared.Failure;
import io.vavr.control.Either;

import java.util.UUID;

/**
 * Interface for deleting expenses.
 */
public interface DeleteExpenseUseCase {

    /**
     * Deletes an expense with the provided ID.
     *
     * @param expenseId The ID of the expense to be deleted.
     * @return {@link Either} a {@link Failure} instance if the expense deletion fails, or null if the deletion is successful.
     */
    Either<Failure, Void> delete(UUID expenseId);

}
