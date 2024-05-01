package io.myfinbox.spendingplan.domain;

import io.hypersistence.utils.hibernate.type.money.MonetaryAmountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CompositeType;

import javax.money.MonetaryAmount;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static io.myfinbox.shared.Guards.*;
import static jakarta.persistence.CascadeType.ALL;
import static lombok.AccessLevel.PACKAGE;

@Entity
@Table(name = "spending_jars")
@Getter
@ToString(exclude = {"plan", "jarExpenseCategories"})
@EqualsAndHashCode(of = {"id", "name"})
@NoArgsConstructor(access = PACKAGE, force = true)
public class Jar {

    public static final int MAX_NAME_LENGTH = 255;

    @EmbeddedId
    private final JarIdentifier id;

    private final Instant creationTimestamp;

    private String name;
    private String description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "percentage"))
    private Percentage percentage;

    @AttributeOverride(name = "amount", column = @Column(name = "amount_to_reach"))
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    @CompositeType(MonetaryAmountType.class)
    private MonetaryAmount amountToReach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", referencedColumnName = "id", nullable = false)
    private Plan plan;

    @OneToMany(mappedBy = "jar", cascade = ALL, orphanRemoval = true)
    private List<JarExpenseCategory> jarExpenseCategories;

    @Builder
    public Jar(Percentage percentage,
               MonetaryAmount amountToReach,
               String name,
               String description,
               Plan plan) {
        this.id = new JarIdentifier(UUID.randomUUID());
        this.creationTimestamp = Instant.now();
        setPercentage(percentage);
        setAmountToReach(amountToReach);
        setName(name);
        this.plan = notNull(plan, "plan cannot be null.");
        this.description = description;
    }

    private void setPercentage(Percentage percentage) {
        this.percentage = notNull(percentage, "percentage cannot be null.");
    }

    private void setName(String name) {
        notBlank(name, "name cannot be blank");
        this.name = doesNotOverflow(name, MAX_NAME_LENGTH, "name overflow, max length allowed '%d'".formatted(MAX_NAME_LENGTH));
    }

    private void setAmountToReach(MonetaryAmount amountToReach) {
        this.amountToReach = greaterThanZero(amountToReach, "amountToReach must be greater than 0.");
    }

    public BigDecimal getAmountToReachAsNumber() {
        return amountToReach.getNumber().numberValue(BigDecimal.class)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public String getCurrencyCode() {
        return amountToReach.getCurrency().getCurrencyCode();
    }

    public record Percentage(Integer value) implements Serializable {

        public Percentage {
            notNull(value, "value cannot be null.");
            if (value <= 0 || value > 100)
                throw new IllegalArgumentException("value must be between 1 and 100.");
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }
}
