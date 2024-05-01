package io.myfinbox.account;

import io.myfinbox.shared.DomainEvent;
import lombok.Builder;

import java.util.UUID;

import static io.myfinbox.shared.Guards.notBlank;
import static io.myfinbox.shared.Guards.notNull;

/**
 * Represents a domain event for the creation of an account.
 *
 * <p>This record captures information about the creation of an account, including its unique identifier,
 * email address, first name, and last name.</p>
 */
@Builder
public record AccountCreated(UUID accountId,
                             String emailAddress,
                             String firstName,
                             String lastName) implements DomainEvent {

    /**
     * Constructor for the AccountCreated record.
     *
     * @param accountId    The unique identifier of the account.
     * @param emailAddress The email address associated with the account.
     * @param firstName    The first name of the account holder.
     * @param lastName     The last name of the account holder.
     */
    public AccountCreated {
        notNull(accountId, "accountIdentifier cannot be null");
        notBlank(emailAddress, "emailAddress cannot be blank");
    }
}
