package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.Failure;
import io.myfinbox.shared.Failure.FieldViolation;
import io.myfinbox.spendingplan.domain.Jar;
import io.myfinbox.spendingplan.domain.Jars;
import io.myfinbox.spendingplan.domain.Plan;
import io.myfinbox.spendingplan.domain.Plans;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static io.myfinbox.spendingplan.application.JarCommand.FIELD_PERCENTAGE;
import static io.myfinbox.spendingplan.domain.Plan.PlanIdentifier;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Objects.isNull;

@Service
@Transactional
@RequiredArgsConstructor
class CreateJarService implements CreateJarUseCase {

    public static final String VALIDATION_CREATE_FAILURE_MESSAGE = "Failed to validate the request to create a spending jar.";
    public static final String JAR_NAME_DUPLICATE_MESSAGE = "Jar name '%s' already exists for the provided spending plan.";
    public static final String PLAN_NOT_FOUND_MESSAGE = "Spending plan was not found.";

    private final JarCommandValidator validator = new JarCommandValidator();

    private final Plans plans;
    private final Jars jars;

    @Override
    public Either<Failure, Jar> create(UUID planId, JarCommand command) {
        var validation = validator.validate(command);
        if (validation.isInvalid()) {
            return Either.left(Failure.ofValidation(VALIDATION_CREATE_FAILURE_MESSAGE, validation.getError().toJavaList()));
        }

        if (isNull(planId)) {
            return Either.left(Failure.ofNotFound(PLAN_NOT_FOUND_MESSAGE));
        }

        // find the plan
        var plan = plans.findByIdEagerJars(new PlanIdentifier(planId));
        if (plan.isEmpty()) {
            return Either.left(Failure.ofNotFound(PLAN_NOT_FOUND_MESSAGE));
        }

        // check if jar name exists as jar to provided plan
        if (jars.existsByNameAndPlan(command.name(), plan.get())) {
            return Either.left(Failure.ofConflict(JAR_NAME_DUPLICATE_MESSAGE.formatted(command.name())));
        }

        // validate percentage total, must be up 100%
        var existingTotalPercentage = plan.get().totalJarPercentage();
        if (isInvalidTotalPercentage(existingTotalPercentage, command.percentage())) {
            return Either.left(maxAllowedPercentageViolation(command.percentage(), 100 - existingTotalPercentage));
        }

        var jar = Jar.builder()
                .name(command.name())
                .percentage(new Jar.Percentage(command.percentage()))
                .amountToReach(amountToReach(command.percentage(), plan.get()))
                .description(command.description())
                .plan(plan.get())
                .build();

        jars.save(jar);

        return Either.right(jar);
    }

    private Failure maxAllowedPercentageViolation(Integer percentage, int maxAllowedJarPercentage) {
        return Failure.ofValidation(VALIDATION_CREATE_FAILURE_MESSAGE, List.of(FieldViolation.builder()
                .field(FIELD_PERCENTAGE)
                .message("Maximum available percentage '%d'.".formatted(maxAllowedJarPercentage))
                .rejectedValue(percentage)
                .build()));
    }

    private MonetaryAmount amountToReach(Integer percentage, Plan plan) {
        BigDecimal percentageDecimal = BigDecimal.valueOf(percentage);
        BigDecimal planAmountDecimal = plan.getAmountAsNumber();

        BigDecimal result = planAmountDecimal.multiply(percentageDecimal.divide(BigDecimal.valueOf(100), 2, HALF_UP));

        return Money.of(result, plan.getCurrencyCode());
    }

    private boolean isInvalidTotalPercentage(int totalExistingPercentage, Integer percentage) {
        return totalExistingPercentage + percentage > 100;
    }
}
