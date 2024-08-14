package io.myfinbox.spendingplan.application;

import io.myfinbox.spendingplan.domain.Jar;

import java.util.List;
import java.util.UUID;

/**
 * Represents a query interface for searching and retrieving jars.
 */
public interface JarQuery {

    /**
     * Initiates a search for jars.
     *
     * @return a builder to further customize the jar search.
     */
    JarQueryBuilder search();

    /**
     * Builder interface for constructing and executing a jar query.
     */
    interface JarQueryBuilder {

        /**
         * Filters the jars by the specified plan ID.
         *
         * @param planId the unique identifier of the plan.
         * @return the updated query builder.
         */
        JarQueryBuilder withPlanId(UUID planId);

        /**
         * Filters the jars by the specified jar ID.
         *
         * @param jarId the unique identifier of the jar.
         * @return the updated query builder.
         */
        JarQueryBuilder withJarId(UUID jarId);

        /**
         * Executes the query and returns a list of jars matching the criteria.
         *
         * @return a list of jars matching the query criteria.
         */
        List<Jar> list();
    }
}

