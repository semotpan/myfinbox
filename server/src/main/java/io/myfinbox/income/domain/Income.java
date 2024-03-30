package io.myfinbox.income.domain;

import io.hypersistence.utils.hibernate.type.money.MonetaryAmountType;
import io.myfinbox.shared.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CompositeType;
import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static io.myfinbox.shared.Guards.greaterThanZero;
import static io.myfinbox.shared.Guards.notNull;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

/**
 * The {@link Income} class represents an income aggregate.
 * It is mapped to the "incomes" table in the database.
 */
@Entity
@Table(name = "incomes")
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = PRIVATE, force = true) // JPA compliant
public class Income {

    @EmbeddedId
    private final IncomeIdentifier id;
    private final Instant creationTimestamp;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "account_id"))
    private final AccountIdentifier account;

    @AttributeOverride(name = "amount", column = @Column(name = "amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    @CompositeType(MonetaryAmountType.class)
    private MonetaryAmount amount;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    private LocalDate incomeDate;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private IncomeSource incomeSource;

    @Builder
    public Income(AccountIdentifier account,
                  MonetaryAmount amount,
                  PaymentType paymentType,
                  LocalDate incomeDate,
                  String description,
                  IncomeSource incomeSource) {
        this.id = new IncomeIdentifier(UUID.randomUUID());
        this.creationTimestamp = Instant.now();
        this.account = notNull(account, "account cannot be null.");
        this.amount = greaterThanZero(amount, "amount must be greater than 0.");
        this.incomeSource = notNull(incomeSource, "incomeSource cannot be null");
        this.paymentType = paymentType == null ? PaymentType.CARD : paymentType;
        this.incomeDate = incomeDate == null ? LocalDate.now() : incomeDate;
        this.description = description;
    }

    private MonetaryAmount requireValidAmount(MonetaryAmount amount) {
        requireNonNull(amount, "amount cannot be null");

        if (amount.isLessThanOrEqualTo(Money.of(BigDecimal.ZERO, amount.getCurrency()))) {
            throw new IllegalArgumentException("amount must be positive value");
        }

        return amount;
    }

    public BigDecimal getAmountAsNumber() {
        return amount.getNumber().numberValue(BigDecimal.class);
    }

    public String getCurrencyCode() {
        return amount.getCurrency().getCurrencyCode();
    }

    @Embeddable
    public record IncomeIdentifier(UUID id) implements Serializable {

        public IncomeIdentifier {
            notNull(id, "id cannot be null");
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }
}
