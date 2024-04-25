package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.Failure;
import io.myfinbox.spendingplan.domain.*;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static io.myfinbox.spendingplan.domain.Plan.PlanIdentifier;
import static java.util.Objects.isNull;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
class AddOrRemoveJarCategoryService implements AddOrRemoveJarCategoryUseCase {

    static final String VALIDATION_CREATE_FAILURE_MESSAGE = "Failed to validate the request to add or remove categories to plan jar.";
    static final String PLAN_NOT_FOUND_MESSAGE = "Spending plan was not found.";
    static final String JAR_NOT_FOUND_MESSAGE = "Spending jar was not found.";
    static final String PLAN_JAR_NOT_FOUND_MESSAGE = "Spending plan jar was not found.";

    private final CategoriesJarCommandValidator validator = new CategoriesJarCommandValidator();

    private final Jars jars;
    private final JarExpenseCategories jarExpenseCategories;

    @Override
    public Either<Failure, List<JarExpenseCategory>> addOrRemove(UUID planId, UUID jarId, JarCategoriesCommand command) {
        var either = validate(planId, jarId, command);
        if (either.isLeft()) {
            return either;
        }

        // require plan jar to exist
        var possibleJar = jars.findByIdAndPlanId(new JarIdentifier(jarId), new PlanIdentifier(planId));
        if (possibleJar.isEmpty()) {
            return Either.left(Failure.ofNotFound(PLAN_JAR_NOT_FOUND_MESSAGE));
        }

        // select unchecked and delete
        var toDeleteCategories = command.categories().stream()
                .filter(jarCategoryToAddOrRemove -> !jarCategoryToAddOrRemove.toAdd())
                .map(JarCategoryToAddOrRemove::categoryId)
                .toList();

        toDeleteCategories.forEach(category ->
                jarExpenseCategories.deleteByJarIdAndCategoryId(new JarIdentifier(jarId), new CategoryIdentifier(category)));

        // select checked and create if not exists
        var toCreateCategories = filterToCreate(possibleJar.get(), command);

        jarExpenseCategories.saveAll(toCreateCategories);

        log.debug("Jar expense category {} were created", toCreateCategories);
        log.debug("Jar expense category {} were deleted", toDeleteCategories);

        return Either.right(toCreateCategories);
    }

    private Either<Failure, List<JarExpenseCategory>> validate(UUID planId, UUID jarId, JarCategoriesCommand command) {
        // schema validation
        var failure = validator.validate(command.categories());
        if (failure.isInvalid()) {
            return Either.left(Failure.ofValidation(VALIDATION_CREATE_FAILURE_MESSAGE, List.of(failure.getError())));
        }

        // validate non null plan id
        if (isNull(planId)) {
            return Either.left(Failure.ofNotFound(PLAN_NOT_FOUND_MESSAGE));
        }

        // validate non null jar id
        if (isNull(jarId)) {
            return Either.left(Failure.ofNotFound(JAR_NOT_FOUND_MESSAGE));
        }

        return Either.right(null);
    }

    private List<JarExpenseCategory> filterToCreate(Jar jar, JarCategoriesCommand command) {
        return command.categories().stream()
                .filter(JarCategoryToAddOrRemove::toAdd)
                .map(JarCategoryToAddOrRemove::categoryId)
                .filter(category -> !existsByJarIdAndCategoryId(jar.getId(), category))
                .map(category -> JarExpenseCategory.builder()
                        .categoryId(new CategoryIdentifier(category))
                        .jar(jar)
                        .build())
                .toList();
    }

    private boolean existsByJarIdAndCategoryId(JarIdentifier jarId, UUID category) {
        return jarExpenseCategories.existsByJarIdAndCategoryId(jarId, new CategoryIdentifier(category));
    }
}
