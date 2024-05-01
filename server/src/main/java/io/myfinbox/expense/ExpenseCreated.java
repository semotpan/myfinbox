package io.myfinbox.expense;

import io.myfinbox.shared.DomainEvent;
import io.myfinbox.shared.PaymentType;
import lombok.Builder;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.UUID;

import static io.myfinbox.shared.Guards.notNull;

/**
 * Represents a domain event for the creation of an expense.
 *
 * <p>This record captures information about the creation of an expense, including its unique identifier,
 * the associated account and category, the amount, date, and payment type.</p>
 */
@Builder
public record ExpenseCreated(UUID expenseId,
                             UUID accountId,
                             UUID categoryId,
                             MonetaryAmount amount,
                             LocalDate expenseDate,
                             PaymentType paymentType) implements DomainEvent {

    /**
     * Constructor for the ExpenseCreated record.
     *
     * @param expenseId   The unique identifier of the expense.
     * @param accountId   The identifier of the account associated with the expense.
     * @param categoryId  The identifier of the category associated with the expense.
     * @param amount      The amount of the expense.
     * @param expenseDate The date of the expense.
     * @param paymentType The payment type of the expense.
     */
    public ExpenseCreated {
        notNull(expenseId, "expenseId cannot be null.");
        notNull(accountId, "accountId cannot be null.");
        notNull(categoryId, "categoryId cannot be null.");
        notNull(amount, "amount cannot be null.");
        notNull(expenseDate, "expenseDate cannot be null.");
        notNull(paymentType, "paymentType cannot be null.");
    }
}
