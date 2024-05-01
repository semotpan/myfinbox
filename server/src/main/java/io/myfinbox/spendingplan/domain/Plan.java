package io.myfinbox.spendingplan.domain;

import io.hypersistence.utils.hibernate.type.money.MonetaryAmountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CompositeType;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.money.MonetaryAmount;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.myfinbox.shared.Guards.*;
import static jakarta.persistence.CascadeType.ALL;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PACKAGE;

@Entity
@Table(name = "spending_plans")
@Getter
@ToString(exclude = "jars")
@EqualsAndHashCode(callSuper = false, of = {"id", "name", "account"})
@NoArgsConstructor(access = PACKAGE, force = true)
public class Plan extends AbstractAggregateRoot<Plan> {

    public static final int MAX_NAME_LENGTH = 255;

    @EmbeddedId
    private final PlanIdentifier id;

    private final Instant creationTimestamp;

    private String name;
    private String description;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "account_id"))
    private final AccountIdentifier account;

    @AttributeOverride(name = "amount", column = @Column(name = "amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    @CompositeType(MonetaryAmountType.class)
    private MonetaryAmount amount;

    @OneToMany(mappedBy = "plan", cascade = ALL, orphanRemoval = true)
    private final List<Jar> jars = new ArrayList<>();

    @Builder
    public Plan(AccountIdentifier account,
                MonetaryAmount amount,
                String name,
                String description) {
        this.id = new PlanIdentifier(UUID.randomUUID());
        this.creationTimestamp = Instant.now();
        this.account = notNull(account, "account cannot be null");
        setAmount(amount);
        setName(name);
        this.description = description;
    }

    private void setName(String name) {
        notBlank(name, "name cannot be blank");
        this.name = doesNotOverflow(name, MAX_NAME_LENGTH, "name overflow, max length allowed '%d'".formatted(MAX_NAME_LENGTH));
    }

    private void setAmount(MonetaryAmount amount) {
        this.amount = greaterThanZero(amount, "amount must be greater than 0.");
    }

    public BigDecimal getAmountAsNumber() {
        return amount.getNumber().numberValue(BigDecimal.class);
    }

    public String getCurrencyCode() {
        return amount.getCurrency().getCurrencyCode();
    }

    public int totalJarPercentage() {
        return jars
                .stream()
                .map(Jar::getPercentage)
                .mapToInt(Jar.Percentage::value)
                .sum();
    }

    @Embeddable
    public record PlanIdentifier(UUID id) implements Serializable {

        public PlanIdentifier {
            requireNonNull(id, "id cannot be null");
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }
}
