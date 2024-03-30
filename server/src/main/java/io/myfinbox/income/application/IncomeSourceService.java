package io.myfinbox.income.application;

import io.myfinbox.income.domain.AccountIdentifier;
import io.myfinbox.income.domain.IncomeSource;
import io.myfinbox.shared.Failure;
import io.vavr.control.Either;

import java.util.List;
import java.util.UUID;

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

    /**
     * Creates an income source based on the provided command.
     *
     * @param command The command containing income source creation details.
     * @return {@link Either} a {@link Failure} instance if the category creation fails, or the created {@link IncomeSource} instance.
     */
    Either<Failure, IncomeSource> create(IncomeSourceCommand command);

    /**
     * Updates an income source name based on the provided command.
     *
     * @param command The command containing income source update details.
     * @return {@link Either} a {@link Failure} instance if the income source update fails, or the update {@link IncomeSource} instance.
     */
    Either<Failure, IncomeSource> update(UUID incomeSourceId, IncomeSourceCommand command);

    /**
     * Deletes an income source based on the provided income source ID.
     *
     * @param incomeSourceId The ID of the income source to delete.
     * @return {@link Either} a {@link Failure} instance if the income source deletion fails, or {@code null} if successful.
     */
    Either<Failure, Void> delete(UUID incomeSourceId);

    record IncomeSourceCommand(String name, UUID accountId) {

        public static final String FIELD_NAME = "name";
        public static final String FIELD_ACCOUNT_ID = "accountId";
    }
}
