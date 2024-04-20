package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.Failure;
import io.myfinbox.spendingplan.domain.JarExpenseCategory;
import io.vavr.control.Either;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

/**
 * Interface representing a use case for adding or removing categories to/from a plan jar.
 */
public interface AddOrRemoveJarCategoryUseCase {

    /**
     * Adds or removes categories to/from a specified jar within a spending plan jar.
     *
     * @param planId  The ID of the spending plan containing the jar.
     * @param jarId   The ID of the jar to which categories will be added or removed.
     * @param command The command containing information about the categories to add or remove.
     * @return An {@link Either} containing either a {@link Failure} if the operation fails
     * or a list of added {@link JarExpenseCategory} after the operation.
     */
    Either<Failure, List<JarExpenseCategory>> addOrRemove(UUID planId, UUID jarId, JarCategoriesCommand command);

    record JarCategoriesCommand(List<JarCategoryToAddOrRemove> categories) {

        public static final String CATEGORIES_JAR_FIELD = "categories";
    }

    record JarCategoryToAddOrRemove(UUID categoryId, Boolean toAdd) {

        /**
         * Gets whether the category is checked or not.
         * Null is treated as true.
         *
         * @return True if the category is checked, false otherwise.
         */
        public Boolean toAdd() { // null treated as true
            return isNull(toAdd) || toAdd;
        }
    }
}
