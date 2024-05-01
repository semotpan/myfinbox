package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.Failure;
import io.myfinbox.spendingplan.domain.Plan;
import io.vavr.control.Either;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Use case interface for creating classic plans.
 */
public interface ClassicPlanBuilderUseCase {

    /**
     * Creates a classic plan based on the provided command.
     *
     * @param command The command containing information for creating the classic plan.
     * @return Either a failure or the created plan.
     */
    Either<Failure, Plan> create(CreateClassicPlanCommand command);

    @Builder
    record CreateClassicPlanCommand(UUID accountId,
                                    BigDecimal amount,
                                    String currencyCode) {
        public static final String FIELD_ACCOUNT_ID = "accountId";
        public static final String FIELD_AMOUNT = "amount";
        public static final String FIELD_CURRENCY_CODE = "currencyCode";
    }
}
