package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.Failure;
import io.myfinbox.spendingplan.domain.Jar;
import io.vavr.control.Either;

import java.util.UUID;

/**
 * Interface representing a use case for creating a new jar.
 */
public interface CreateJarUseCase {

    /**
     * Attempts to create a new jar for a given spending plan.
     *
     * @param planId  The ID of the spending plan to which the new jar will be added.
     * @param command The command containing information about the jar to be created.
     * @return An {@link Either} containing either a {@link Failure} if the creation fails or the created {@link Jar}.
     */
    Either<Failure, Jar> create(UUID planId, JarCommand command);

}
