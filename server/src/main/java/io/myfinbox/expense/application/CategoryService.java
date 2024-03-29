package io.myfinbox.expense.application;

import io.myfinbox.expense.domain.AccountIdentifier;
import io.myfinbox.expense.domain.Category;
import io.myfinbox.shared.Failure;
import io.vavr.control.Either;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing categories.
 */
// FIXME: ensure the current auth user matches the accountID
public interface CategoryService {

    /**
     * Initializes default categories for the specified account.
     *
     * @param account The account for which default categories are to be initialized.
     * @return {@link Either} a {@link Failure} instance if initialization fails, or a list of initialized categories.
     */
    Either<Failure, List<Category>> createDefault(AccountIdentifier account);

    /**
     * Creates a category based on the provided command.
     *
     * @param command The command containing category creation details.
     * @return {@link Either} a {@link Failure} instance if the category creation fails, or the created {@link Category} instance.
     */
    Either<Failure, Category> create(CategoryCommand command);

    /**
     * Updates a category name based on the provided command.
     *
     * @param command The command containing category update details.
     * @return {@link Either} a {@link Failure} instance if the category update fails, or the update {@link Category} instance.
     */
    Either<Failure, Category> update(UUID categoryId, CategoryCommand command);

    /**
     * Deletes a category based on the provided category ID.
     *
     * @param categoryId The ID of the category to delete.
     * @return {@link Either} a {@link Failure} instance if the category deletion fails, or {@code null} if successful.
     */
    Either<Failure, Void> delete(UUID categoryId);

    record CategoryCommand(String name, UUID accountId) {

        public static final String FIELD_NAME = "name";
        public static final String FIELD_ACCOUNT_ID = "accountId";
    }
}
