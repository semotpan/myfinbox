package io.myfinbox.income;

import io.myfinbox.shared.DomainEvent;
import io.myfinbox.shared.PaymentType;
import lombok.Builder;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.UUID;

import static io.myfinbox.shared.Guards.notNull;

@Builder
public record IncomeCreated(UUID incomeId,
                            UUID accountId,
                            UUID incomeSourceId,
                            MonetaryAmount amount,
                            LocalDate incomeDate,
                            PaymentType paymentType) implements DomainEvent {

    public IncomeCreated {
        notNull(incomeId, "incomeId cannot be null.");
        notNull(accountId, "accountId cannot be null.");
        notNull(incomeSourceId, "incomeSourceId cannot be null.");
        notNull(amount, "amount cannot be null.");
        notNull(incomeDate, "incomeDate cannot be null.");
        notNull(paymentType, "paymentType cannot be null.");
    }
}
