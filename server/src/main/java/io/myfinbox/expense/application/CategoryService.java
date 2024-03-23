package io.myfinbox.expense.application;

import io.myfinbox.expense.domain.AccountIdentifier;
import io.myfinbox.expense.domain.Category;
import io.myfinbox.shared.Failure;
import io.vavr.control.Either;

import java.util.List;

/**
 * Service interface for managing categories.
 */
public interface CategoryService {

    /**
     * Initializes default categories for the specified account.
     *
     * @param account The account for which default categories are to be initialized.
     * @return {@link Either} a {@link Failure} instance if initialization fails, or a list of initialized categories.
     */
    Either<Failure, List<Category>> createDefault(AccountIdentifier account);

}
