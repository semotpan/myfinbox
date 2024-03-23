package io.myfinbox.account.application;

import io.myfinbox.account.domain.Account;
import io.myfinbox.shared.Failure;
import io.vavr.control.Either;
import lombok.Builder;

/**
 * This interface defines the contract for creating an account.
 */
public interface CreateAccountUseCase {

    /**
     * Creates an account based on the provided command.
     *
     * @param cmd The command containing account creation details.
     * @return {@link Either} a {@link Failure} instance if the account creation fails, or the created {@link  Account} instance.
     */
    Either<Failure, Account> create(CreateAccountCommand cmd);

    @Builder
    record CreateAccountCommand(String firstName,
                                String lastName,
                                String emailAddress) {

        public static final String FIELD_FIRST_NAME = "firstName";
        public static final String FIELD_LAST_NAME = "lastName";
        public static final String FIELD_EMAIL_ADDRESS = "emailAddress";

    }
}
