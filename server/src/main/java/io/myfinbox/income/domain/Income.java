package io.myfinbox.income.domain;

import io.hypersistence.utils.hibernate.type.money.MonetaryAmountType;
import io.myfinbox.income.IncomeCreated;
import io.myfinbox.income.IncomeDeleted;
import io.myfinbox.income.IncomeUpdated;
import io.myfinbox.shared.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CompositeType;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.money.MonetaryAmount;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static io.myfinbox.shared.Guards.greaterThanZero;
import static io.myfinbox.shared.Guards.notNull;
import static lombok.AccessLevel.PRIVATE;

/**
 * The {@link Income} class represents an income aggregate.
 * It is mapped to the "incomes" table in the database.
 */
@Entity
@Table(name = "incomes")
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PRIVATE, force = true) // JPA compliant
public class Income extends AbstractAggregateRoot<Income> {

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

        registerEvent(IncomeCreated.builder()
                .incomeId(this.id.id())
                .accountId(this.account.id())
                .amount(this.amount)
                .incomeSourceId(this.incomeSource.getId().id())
                .paymentType(this.paymentType)
                .incomeDate(this.incomeDate)
                .build());
    }

    public BigDecimal getAmountAsNumber() {
        return amount.getNumber().numberValue(BigDecimal.class);
    }

    public String getCurrencyCode() {
        return amount.getCurrency().getCurrencyCode();
    }

    public void update(IncomeBuilder builder) {
        notNull(builder, "builder cannot be null.");
        this.amount = greaterThanZero(builder.amount, "amount must be greater than 0.");
        this.incomeSource = notNull(builder.incomeSource, "incomeSource cannot be null");
        this.paymentType = builder.paymentType == null ? PaymentType.CARD : builder.paymentType;
        this.incomeDate = builder.incomeDate == null ? LocalDate.now() : builder.incomeDate;
        this.description = builder.description;

        registerEvent(IncomeUpdated.builder()
                .incomeId(this.id.id())
                .accountId(this.account.id())
                .amount(this.amount)
                .incomeSourceId(this.incomeSource.getId().id())
                .paymentType(this.paymentType)
                .incomeDate(this.incomeDate)
                .build());
    }

    public void delete() {
        registerEvent(IncomeDeleted.builder()
                .incomeId(this.id.id())
                .accountId(this.account.id())
                .amount(this.amount)
                .incomeSourceId(this.incomeSource.getId().id())
                .paymentType(this.paymentType)
                .incomeDate(this.incomeDate)
                .build());
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
