package io.myfinbox.account;

import io.myfinbox.account.Account.AccountIdentifier;
import io.myfinbox.account.Account.EmailAddress;
import io.myfinbox.shared.DomainEvent;
import lombok.Builder;

import static java.util.Objects.requireNonNull;

/**
 * Represents an event indicating that an account has been created.
 */
@Builder
public record AccountCreated(AccountIdentifier accountIdentifier,
                             EmailAddress emailAddress,
                             String firstName,
                             String lastName) implements DomainEvent {

    public AccountCreated {
        requireNonNull(accountIdentifier, "accountIdentifier cannot be null");
        requireNonNull(emailAddress, "emailAddress cannot be null");
    }
}
