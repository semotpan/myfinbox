package io.myfinbox.spendingplan.domain;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

import static io.myfinbox.shared.Guards.notNull;

@Embeddable
public record CategoryIdentifier(UUID id) implements Serializable {

    public CategoryIdentifier {
        notNull(id, "id cannot be null");
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
