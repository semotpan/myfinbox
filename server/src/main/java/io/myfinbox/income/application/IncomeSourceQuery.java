package io.myfinbox.income.application;

import io.myfinbox.income.domain.IncomeSource;

import java.util.List;
import java.util.UUID;

/**
 * Interface for querying income source information.
 */
public interface IncomeSourceQuery {

    /**
     * Searches for income sources associated with a specific account.
     *
     * @param accountId the unique identifier of the account to search income sources for
     * @return a list of {@link IncomeSource} objects associated with the specified account
     */
    List<IncomeSource> search(UUID accountId);

}
