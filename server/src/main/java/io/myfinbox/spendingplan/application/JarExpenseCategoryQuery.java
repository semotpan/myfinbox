package io.myfinbox.spendingplan.application;

import io.myfinbox.spendingplan.domain.JarExpenseCategory;

import java.util.List;
import java.util.UUID;

/**
 * Represents a query interface for searching and retrieving jar expense categories.
 */
public interface JarExpenseCategoryQuery {

    /**
     * Searches for jar expense categories based on the specified plan ID and jar ID.
     *
     * @param planId the unique identifier of the plan.
     * @param jarId  the unique identifier of the jar.
     * @return a list of jar expense categories matching the specified plan ID and jar ID.
     */
    List<JarExpenseCategory> search(UUID planId, UUID jarId);

}
