package io.myfinbox.income;

import io.myfinbox.shared.Failure;
import io.vavr.control.Either;

import java.util.List;

/**
 * Service interface for managing income sources.
 */
public interface IncomeSourceService {

    /**
     * Creates default income sources for the specified account.
     *
     * @param account The account for which default income sources are to be created.
     * @return {@link Either} a {@link Failure} instance if creation fails, or a list of created income sources.
     */
    Either<Failure, List<IncomeSource>> createDefault(AccountIdentifier account);

}

