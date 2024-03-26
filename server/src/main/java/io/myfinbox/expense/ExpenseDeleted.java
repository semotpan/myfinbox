package io.myfinbox.expense;

import io.myfinbox.shared.DomainEvent;
import io.myfinbox.shared.PaymentType;
import lombok.Builder;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.UUID;

import static io.myfinbox.shared.Guards.notNull;

@Builder
public record ExpenseDeleted(UUID expenseId,
                             UUID accountId,
                             UUID categoryId,
                             MonetaryAmount amount,
                             LocalDate expenseDate,
                             PaymentType paymentType) implements DomainEvent {

    public ExpenseDeleted {
        notNull(expenseId, "expenseId cannot be null.");
        notNull(accountId, "accountId cannot be null.");
        notNull(categoryId, "categoryId cannot be null.");
        notNull(amount, "amount cannot be null.");
        notNull(expenseDate, "expenseDate cannot be null.");
        notNull(paymentType, "paymentType cannot be null.");
    }
}
