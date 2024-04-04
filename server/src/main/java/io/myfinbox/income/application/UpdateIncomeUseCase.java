package io.myfinbox.income.application;

import io.myfinbox.income.domain.Income;
import io.myfinbox.shared.Failure;
import io.vavr.control.Either;

import java.util.UUID;

/**
 * Interface for updating incomes.
 */
public interface UpdateIncomeUseCase {

    /**
     * Updates an income with the provided ID based on the provided command.
     *
     * @param incomeId The ID of the income to be updated.
     * @param command  The command containing income updating details.
     * @return @link Either} a {@link Failure} instance if the income updating fails, or the updated {@link Income} instance.
     */
    Either<Failure, Income> update(UUID incomeId, IncomeCommand command);

}
