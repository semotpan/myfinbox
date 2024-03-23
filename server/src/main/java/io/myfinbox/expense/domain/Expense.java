package io.myfinbox.expense.domain;

import io.hypersistence.utils.hibernate.type.money.MonetaryAmountType;
import io.myfinbox.expense.ExpenseCreated;
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
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

@Entity
@Table(name = "expenses")
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PRIVATE, force = true)
public class Expense extends AbstractAggregateRoot<Expense> {

    @EmbeddedId
    private final ExpenseIdentifier id;

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

    private LocalDate expenseDate;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @Builder
    public Expense(AccountIdentifier account,
                   MonetaryAmount amount,
                   PaymentType paymentType,
                   LocalDate expenseDate,
                   String description,
                   Category category) {
        this.id = new ExpenseIdentifier(UUID.randomUUID());
        this.creationTimestamp = Instant.now();
        this.account = notNull(account, "account cannot be null.");
        this.amount = greaterThanZero(amount, "amount must be greater than 0.");
        this.category = notNull(category, "category cannot be null.");
        this.paymentType = isNull(paymentType) ? PaymentType.CARD : paymentType;
        this.expenseDate = isNull(expenseDate) ? LocalDate.now() : expenseDate;
        this.description = description;

        registerEvent(ExpenseCreated.builder()
                .expenseId(this.id.id())
                .accountId(this.account.id())
                .categoryId(this.category.getId().id())
                .amount(this.amount)
                .expenseDate(this.expenseDate)
                .paymentType(this.paymentType)
                .build());
    }

    public BigDecimal getAmountAsNumber() {
        return amount.getNumber().numberValue(BigDecimal.class);
    }

    public String getCurrencyCode() {
        return amount.getCurrency().getCurrencyCode();
    }

    @Embeddable
    public record ExpenseIdentifier(UUID id) implements Serializable {

        public ExpenseIdentifier {
            notNull(id, "id cannot be null.");
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }
}
