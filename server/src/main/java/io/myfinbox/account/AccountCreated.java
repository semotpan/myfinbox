package io.myfinbox.account;

import io.myfinbox.shared.DomainEvent;
import lombok.Builder;

import java.util.UUID;

import static io.myfinbox.shared.Guards.notBlank;
import static io.myfinbox.shared.Guards.notNull;

/**
 * Represents an event indicating that an account has been created.
 */
@Builder
public record AccountCreated(UUID accountId,
                             String emailAddress,
                             String firstName,
                             String lastName) implements DomainEvent {

    public AccountCreated {
        notNull(accountId, "accountIdentifier cannot be null");
        notBlank(emailAddress, "emailAddress cannot be blank");
    }
}
