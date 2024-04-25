package io.myfinbox.spendingplan.domain;

import io.hypersistence.utils.hibernate.type.money.MonetaryAmountType;
import io.myfinbox.shared.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CompositeType;

import javax.money.MonetaryAmount;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static io.myfinbox.shared.Guards.notNull;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PACKAGE;

@Entity
@Getter
@ToString
@Table(name = "jar_expense_record")
@EqualsAndHashCode(of = {"expenseId", "jarExpenseCategory"})
@NoArgsConstructor(access = PACKAGE, force = true)
public class ExpenseRecord {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "expense_id"))
    private final ExpenseIdentifier expenseId;
    private final Instant creationTimestamp;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "category_id"))
    private CategoryIdentifier categoryId;

    @AttributeOverride(name = "amount", column = @Column(name = "amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    @CompositeType(MonetaryAmountType.class)
    private MonetaryAmount amount;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    private LocalDate expenseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jar_expense_category_id", referencedColumnName = "id", nullable = false)
    private JarExpenseCategory jarExpenseCategory;

    @Builder
    public ExpenseRecord(ExpenseIdentifier expenseId,
                         CategoryIdentifier categoryId,
                         MonetaryAmount amount,
                         PaymentType paymentType,
                         LocalDate expenseDate,
                         JarExpenseCategory jarExpenseCategory) {
        this.expenseId = notNull(expenseId, "expenseId cannot be null.");
        this.categoryId = notNull(categoryId, "categoryId cannot be null.");
        this.amount = notNull(amount, "amount cannot be null.");
        this.paymentType = notNull(paymentType, "paymentType cannot be null.");
        this.expenseDate = notNull(expenseDate, "expenseDate cannot be null.");
        this.jarExpenseCategory = notNull(jarExpenseCategory, "jarExpenseCategory cannot be null.");
        this.creationTimestamp = Instant.now();
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
