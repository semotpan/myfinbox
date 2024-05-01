package io.myfinbox.spendingplan.application

import io.myfinbox.shared.Failure
import io.vavr.control.Either
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.spendingplan.DataSamples.newSampleClassicCreatePlanCommand
import static io.myfinbox.spendingplan.DataSamples.newSampleJar
import static io.myfinbox.spendingplan.DataSamples.newSamplePlan

@Tag("unit")
class ClassicPlanBuilderServiceSpec extends Specification {

    CreatePlanUseCase createPlanUseCase
    CreateJarUseCase createJarUseCase
    ClassicPlanBuilderService service

    def setup() {
        createPlanUseCase = Mock()
        createJarUseCase = Mock()
        service = new ClassicPlanBuilderService(createPlanUseCase, createJarUseCase)
    }

    def "should fail classic plan creation when plan validation failure"() {
        given: 'a plan validation failure'
        1 * createPlanUseCase.create(_ as PlanCommand) >> Either.left(
                Failure.ofValidation("Plan validation failure", [
                        Failure.FieldViolation.builder()
                                .field('accountId')
                                .message('AccountId cannot be null.')
                                .build()
                ])
        )

        when: 'attempting to create a classic plan'
        def either = service.create(newSampleClassicCreatePlanCommand())

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for create plan request'
        assert either.getLeft() == Failure.ofValidation("Plan validation failure", [
                Failure.FieldViolation.builder()
                        .field('accountId')
                        .message('AccountId cannot be null.')
                        .build()
        ])
    }

    def "should fail classic plan creation when jar validation failure"() {
        given: 'a valid plan and a jar validation failure'
        1 * createPlanUseCase.create(_ as PlanCommand) >> Either.right(newSamplePlan())
        1 * createJarUseCase.create(_ as UUID, _ as JarCommand) >> Either.left(
                Failure.ofValidation("Jar validation failure", [
                        Failure.FieldViolation.builder()
                                .field('percentage')
                                .message('Percentage cannot be null.')
                                .build()
                ])
        )

        when: 'attempting to create a classic plan'
        def either = service.create(newSampleClassicCreatePlanCommand())

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for create plan request'
        assert either.getLeft() == Failure.ofValidation("Jar validation failure", [
                Failure.FieldViolation.builder()
                        .field('percentage')
                        .message('Percentage cannot be null.')
                        .build()
        ])
    }

    def "should create a classic plan creation successfully"() {
        given: 'a valid plan and valid jars'
        1 * createPlanUseCase.create(_ as PlanCommand) >> Either.right(newSamplePlan())
        6 * createJarUseCase.create(_ as UUID, _ as JarCommand) >> Either.right(newSampleJar())

        when: 'attempting to create a classic plan'
        def either = service.create(newSampleClassicCreatePlanCommand())

        then: 'plan value is present'
        assert either.isRight()

        and: 'plan is build as expected'
        assert either.get() == newSamplePlan()
    }
}
