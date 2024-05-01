package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.Failure;
import io.myfinbox.spendingplan.domain.ClassicJarDistribution;
import io.myfinbox.spendingplan.domain.Plan;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
class ClassicPlanBuilderService implements ClassicPlanBuilderUseCase {

    static final String CLASSIC_SPENDING_PLAN = "My classic spending plan";
    static final String CLASSIC_PLAN_DESCRIPTION = "My classic plan distribution: Necessities(55%), Long Term Savings(10%), " +
            "Education(10%), Play(10%), Financial(10%), Give(5%).";

    private final CreatePlanUseCase createPlanUseCase;
    private final CreateJarUseCase createJarUseCase;

    @Override
    public Either<Failure, Plan> create(CreateClassicPlanCommand command) {
        log.debug("Classic spending plan creation ...");

        var planCommand = PlanCommand.builder()
                .accountId(command.accountId())
                .amount(command.amount())
                .currencyCode(command.currencyCode())
                .name(CLASSIC_SPENDING_PLAN)
                .description(CLASSIC_PLAN_DESCRIPTION)
                .build();

        var planEither = createPlanUseCase.create(planCommand);
        if (planEither.isLeft()) {
            return planEither;
        }

        var jarCommands = Arrays.stream(ClassicJarDistribution.values())
                .map(jar -> JarCommand.builder()
                        .name(jar.jarName())
                        .percentage(jar.percentage())
                        .description(jar.description())
                        .build())
                .toList();

        var planIdentifier = planEither.get().getId();
        for (var jarCommand : jarCommands) {
            var jarEither = createJarUseCase.create(planIdentifier.id(), jarCommand);
            if (jarEither.isLeft()) {
                return Either.left(jarEither.getLeft());
            }
        }

        log.debug("Classic spending plan was created '{}' , with 6 jars.", CLASSIC_SPENDING_PLAN);
        return Either.right(planEither.get());
    }
}
