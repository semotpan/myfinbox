package io.myfinbox.spendingplan.domain;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

import static io.myfinbox.shared.Guards.notNull;

/**
 * Represents an identifier for an account.
 */
@Embeddable
public record AccountIdentifier(UUID id) implements Serializable {

    public AccountIdentifier {
        notNull(id, "id cannot be null");
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
