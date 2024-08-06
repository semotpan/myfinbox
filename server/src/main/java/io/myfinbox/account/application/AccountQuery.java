package io.myfinbox.account.application;

import io.myfinbox.account.domain.Account;
import io.myfinbox.shared.Failure;
import io.vavr.control.Either;

import java.util.UUID;

/**
 * Interface for querying account information.
 */
public interface AccountQuery {

    /**
     * Finds an account by its unique identifier.
     *
     * @param accountId the unique identifier of the account to find
     * @return an {@link Either} instance containing either a {@link Failure} or the {@link Account} found
     */
    Either<Failure, Account> findOne(UUID accountId);
}
