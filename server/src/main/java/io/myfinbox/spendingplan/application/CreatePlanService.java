package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.Failure;
import io.myfinbox.spendingplan.domain.AccountIdentifier;
import io.myfinbox.spendingplan.domain.Plan;
import io.myfinbox.spendingplan.domain.Plans;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
class CreatePlanService implements CreatePlanUseCase {

    public static final String VALIDATION_CREATE_FAILURE_MESSAGE = "Validation failed for the create spending plan request.";
    public static final String PLAN_NAME_DUPLICATE_MESSAGE = "Spending plan name '%s' already exists.";

    private final PlanCommandValidator validator = new PlanCommandValidator();

    private final Plans plans;

    @Override
    public Either<Failure, Plan> create(PlanCommand command) {
        var validation = validator.validate(command);
        if (validation.isInvalid()) {
            return Either.left(Failure.ofValidation(VALIDATION_CREATE_FAILURE_MESSAGE, validation.getError().toJavaList()));
        }

        if (plans.existsByNameAndAccount(command.name(), new AccountIdentifier(command.accountId()))) {
            return Either.left(Failure.ofConflict(PLAN_NAME_DUPLICATE_MESSAGE.formatted(command.name())));
        }

        var plan = Plan.builder()
                .name(command.name())
                .account(new AccountIdentifier(command.accountId()))
                .amount(Money.of(command.amount(), command.currencyCode()))
                .description(command.description())
                .build();

        plans.save(plan);

        return Either.right(plan);
    }
}
