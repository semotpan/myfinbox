package io.myfinbox.expense;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@Embeddable
public record AccountIdentifier(UUID id) implements Serializable {

    public AccountIdentifier {
        requireNonNull(id, "id cannot be null");
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
