package io.myfinbox.spendingplan.application;

import io.myfinbox.spendingplan.domain.Plan;

import java.util.List;
import java.util.UUID;

/**
 * Represents a query interface for searching and retrieving plans.
 */
public interface PlanQuery {

    /**
     * Initiates a search for plans.
     *
     * @return a builder to further customize the plan search.
     */
    PlanQueryBuilder search();

    /**
     * Builder interface for constructing and executing a plan query.
     */
    interface PlanQueryBuilder {

        /**
         * Filters the plans by the specified plan ID.
         *
         * @param planId the unique identifier of the plan.
         * @return the updated query builder.
         */
        PlanQueryBuilder withPlanId(UUID planId);

        /**
         * Filters the plans by the specified account ID.
         *
         * @param accountId the unique identifier of the account.
         * @return the updated query builder.
         */
        PlanQueryBuilder withAccountId(UUID accountId);

        /**
         * Executes the query and returns a list of plans matching the criteria.
         *
         * @return a list of plans matching the query criteria.
         */
        List<Plan> list();
    }
}
