package io.myfinbox.income.application;

import io.myfinbox.shared.Failure;
import io.vavr.control.Either;

import java.util.UUID;

/**
 * Interface for deleting incomes.
 */
public interface DeleteIncomeUseCase {

    /**
     * Deletes an income with the provided ID.
     *
     * @param incomeId The ID of the income to be deleted.
     * @return {@link Either} a {@link Failure} instance if the income deletion fails, or null if the deletion is successful.
     */
    Either<Failure, Void> delete(UUID incomeId);

}
