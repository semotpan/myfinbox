package io.myfinbox.income;

import io.myfinbox.shared.DomainEvent;
import io.myfinbox.shared.PaymentType;
import lombok.Builder;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.UUID;

import static io.myfinbox.shared.Guards.notNull;

/**
 * Represents a domain event for the deletion of income.
 *
 * <p>This record captures information about the deletion of income, including its unique identifier,
 * the associated account and income source, the amount, date, and payment type.</p>
 */
@Builder
public record IncomeDeleted(UUID incomeId,
                            UUID accountId,
                            UUID incomeSourceId,
                            MonetaryAmount amount,
                            LocalDate incomeDate,
                            PaymentType paymentType) implements DomainEvent {

    /**
     * Constructor for the IncomeDeleted record.
     *
     * @param incomeId       The unique identifier of the income.
     * @param accountId      The identifier of the account associated with the income.
     * @param incomeSourceId The identifier of the income source associated with the income.
     * @param amount         The amount of the income.
     * @param incomeDate     The date of the income.
     * @param paymentType    The payment type of the income.
     */
    public IncomeDeleted {
        notNull(incomeId, "incomeId cannot be null.");
        notNull(accountId, "accountId cannot be null.");
        notNull(incomeSourceId, "incomeSourceId cannot be null.");
        notNull(amount, "amount cannot be null.");
        notNull(incomeDate, "incomeDate cannot be null.");
        notNull(paymentType, "paymentType cannot be null.");
    }
}
