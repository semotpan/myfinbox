package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.Failure;
import io.myfinbox.spendingplan.domain.AccountIdentifier;
import io.myfinbox.spendingplan.domain.Plan;
import io.myfinbox.spendingplan.domain.Plans;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.util.Objects.isNull;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
class UpdatePlanService implements UpdatePlanUseCase {

    public static final String VALIDATION_UPDATE_FAILURE_MESSAGE = "Validation failed for the update spending plan request.";
    public static final String PLAN_NOT_FOUND_MESSAGE = "Spending plan was not found.";
    public static final String PLAN_NAME_DUPLICATE_MESSAGE = "Spending plan name '%s' already exists.";

    private final PlanCommandValidator validator = new PlanCommandValidator();

    private final Plans plans;

    @Override
    public Either<Failure, Plan> update(UUID planId, PlanCommand command) {
        var validation = validator.validate(command);
        if (validation.isInvalid()) {
            return Either.left(Failure.ofValidation(VALIDATION_UPDATE_FAILURE_MESSAGE, validation.getError().toJavaList()));
        }

        if (isNull(planId)) {
            return Either.left(Failure.ofNotFound(PLAN_NOT_FOUND_MESSAGE));
        }

        var possiblePlan = plans.findByIdEagerJars(new Plan.PlanIdentifier(planId));
        if (possiblePlan.isEmpty()) {
            return Either.left(Failure.ofNotFound(PLAN_NOT_FOUND_MESSAGE));
        }

        // if plan is same return it
        if (possiblePlan.get().same(command.name(), Money.of(command.amount(), command.currencyCode()), command.description())) {
            log.debug("Plan ID {} update skipped, no changes found!", planId);
            return Either.right(possiblePlan.get());
        }

        // check name duplication
        if (!possiblePlan.get().sameName(command.name()) && plans.existsByNameAndAccount(command.name(), new AccountIdentifier(command.accountId()))) {
            return Either.left(Failure.ofConflict(PLAN_NAME_DUPLICATE_MESSAGE.formatted(command.name())));
        }

        possiblePlan.get().update(command.name(), Money.of(command.amount(), command.currencyCode()), command.description());

        plans.save(possiblePlan.get());

        log.debug("Plan ID {} was updated", planId);

        return Either.right(possiblePlan.get());
    }
}
