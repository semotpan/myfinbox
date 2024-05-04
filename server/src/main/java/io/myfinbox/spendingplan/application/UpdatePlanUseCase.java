package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.Failure;
import io.myfinbox.spendingplan.domain.Plan;
import io.vavr.control.Either;

import java.util.UUID;

/**
 * Use case interface for updating plans.
 */
public interface UpdatePlanUseCase {

    /**
     * Updates a plan identified by the provided planId based on the given command.
     *
     * @param planId  The unique identifier of the plan to be updated.
     * @param command The command containing the updated information for the plan.
     * @return Either a failure or the updated plan.
     */
    Either<Failure, Plan> update(UUID planId, PlanCommand command);
}
