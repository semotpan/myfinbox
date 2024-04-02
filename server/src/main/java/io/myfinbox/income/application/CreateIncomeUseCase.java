package io.myfinbox.income.application;

import io.myfinbox.income.domain.Income;
import io.myfinbox.shared.Failure;
import io.vavr.control.Either;

/**
 * Interface for creating incomes.
 */
public interface CreateIncomeUseCase {

    /**
     * Creates an income based on the provided command.
     *
     * @param command The command containing income creation details.
     * @return {@link Either} a {@link Failure} instance if the income creation fails, or the created {@link Income} instance.
     */
    Either<Failure, Income> create(IncomeCommand command);

}
