package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.Failure;
import io.myfinbox.spendingplan.domain.Plan;
import io.vavr.control.Either;

/**
 * Interface for creating plans.
 */
public interface CreatePlanUseCase {

    /**
     * Creates a plan based on the provided command.
     *
     * @param command The command containing plan creation details.
     * @return {@link Either} a {@link Failure} instance if the plan creation fails, or the created {@link Plan} instance.
     */
    Either<Failure, Plan> create(PlanCommand command);

}
