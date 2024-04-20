package io.myfinbox.income.domain;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import static io.myfinbox.shared.Guards.*;
import static lombok.AccessLevel.PRIVATE;

/**
 * Defines the domain model representation of income source
 */
@Entity
@Table(name = "income_source")
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = PRIVATE, force = true)
public final class IncomeSource {

    public static final int NAME_MAX_LENGTH = 100;

    @EmbeddedId
    private final IncomeSourceIdentifier id;
    private final Instant creationTimestamp;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "account_id"))
    private final AccountIdentifier account;

    private String name;

    public IncomeSource(String name, AccountIdentifier account) {
        this.id = new IncomeSourceIdentifier(UUID.randomUUID());
        this.account = notNull(account, "account cannot be null");
        setName(name);
        this.creationTimestamp = Instant.now();
    }

    public boolean sameName(String name) {
        return this.name.equalsIgnoreCase(name);
    }

    public void setName(String name) {
        notBlank(name, "name cannot be blank");
        this.name = doesNotOverflow(name, NAME_MAX_LENGTH, "name overflow, max length allowed '%d'".formatted(NAME_MAX_LENGTH));
    }

    public boolean matches(IncomeSourceIdentifier identifier) {
        return this.id.equals(identifier);
    }

    @Embeddable
    public record IncomeSourceIdentifier(UUID id) implements Serializable {

        public IncomeSourceIdentifier {
            notNull(id, "id cannot be null");
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }
}
