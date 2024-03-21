package io.myfinbox.income;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.length;

/**
 * Defines the domain model representation of income source
 */
@Entity
@Table(name = "incomesource")
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = PRIVATE, force = true)
public final class IncomeSource {

    static final int MAX_LENGTH = 100;

    @EmbeddedId
    private final IncomeSourceIdentifier id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "account_id"))
    private final AccountIdentifier account;

    private String name;
    private Instant creationTimestamp;

    public IncomeSource(String name, AccountIdentifier account) {
        this.id = new IncomeSourceIdentifier(UUID.randomUUID());
        this.account = requireNonNull(account, "account cannot be null");
        this.name = requireValidName(name);
        this.creationTimestamp = Instant.now();
    }

    private String requireValidName(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name cannot be blank");
        }

        if (length(name) > MAX_LENGTH) {
            throw new IllegalArgumentException("name overflow, max length allowed '%d'".formatted(MAX_LENGTH));
        }

        return name;
    }

    @Embeddable
    public record IncomeSourceIdentifier(UUID id) implements Serializable {

        public IncomeSourceIdentifier {
            requireNonNull(id, "id cannot be null");
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }
}
