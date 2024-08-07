package io.myfinbox.expense.application;

import io.myfinbox.expense.domain.Category;

import java.util.List;
import java.util.UUID;

/**
 * Interface for querying category information.
 */
public interface CategoryQuery {

    /**
     * Searches for categories associated with a specific account.
     *
     * @param accountId the unique identifier of the account to search categories for
     * @return a list of {@link Category} objects associated with the specified account
     */
    List<Category> search(UUID accountId);
}

