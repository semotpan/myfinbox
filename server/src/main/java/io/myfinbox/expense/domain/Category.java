package io.myfinbox.expense.domain;

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
 * Defines the domain model representation of expense category
 */
@Entity
@Table(name = "expensecategory")
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = PRIVATE, force = true)
public final class Category {

    public static final int NAME_MAX_LENGTH = 100;

    @EmbeddedId
    private final CategoryIdentifier id;
    private final Instant creationTimestamp;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "account_id"))
    private final AccountIdentifier account;

    private String name;

    public Category(String name, AccountIdentifier account) {
        this.id = new CategoryIdentifier(UUID.randomUUID());
        this.account = notNull(account, "account cannot be null");
        setName(name);
        this.creationTimestamp = Instant.now();
    }

    public void setName(String name) {
        notBlank(name, "name cannot be blank");
        this.name = doesNotOverflow(name, NAME_MAX_LENGTH, "name overflow, max length allowed '%d'".formatted(NAME_MAX_LENGTH));
    }

    public boolean sameName(String name) {
        return this.name.equalsIgnoreCase(name);
    }

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
}
