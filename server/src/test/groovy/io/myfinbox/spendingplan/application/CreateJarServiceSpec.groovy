package io.myfinbox.spendingplan.application

import io.myfinbox.shared.Failure
import io.myfinbox.spendingplan.DataSamples
import io.myfinbox.spendingplan.domain.Jar
import io.myfinbox.spendingplan.domain.Jars
import io.myfinbox.spendingplan.domain.Plan
import io.myfinbox.spendingplan.domain.Plans
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.spendingplan.DataSamples.newSampleCreateJarCommand
import static io.myfinbox.spendingplan.DataSamples.newSamplePlan
import static io.myfinbox.spendingplan.application.CreateJarService.*
import static java.lang.Boolean.FALSE
import static java.lang.Boolean.TRUE
import static org.apache.commons.lang3.RandomStringUtils.random

@Tag("unit")
class CreateJarServiceSpec extends Specification {

    Plans plans
    Jars jars
    CreateJarService service

    def setup() {
        plans = Mock()
        jars = Mock()
        service = new CreateJarService(plans, jars)
    }

    def "should fail jar creation when name is invalid"() {
        given: 'a new command with an invalid name'
        def command = newSampleCreateJarCommand(name: jar)

        when: 'attempting to create a jar with a invalid name'
        def either = service.create(UUID.randomUUID(), command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for create jar request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_CREATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('name')
                        .message(failMessage)
                        .rejectedValue(jar)
                        .build()
        ])

        where:
        jar                     | failMessage
        null                    | 'Name cannot be empty.'
        ''                      | 'Name cannot be empty.'
        '   '                   | 'Name cannot be empty.'
        random(256, true, true) | "Name length cannot exceed ${Jar.MAX_NAME_LENGTH} characters."
    }

    def "should fail jar creation when percentage is invalid"() {
        given: 'a new command with an invalid percentage'
        def command = newSampleCreateJarCommand(percentage: percentage)

        when: 'attempting to create a jar with a invalid percentage'
        def either = service.create(UUID.randomUUID(), command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for create jar request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_CREATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('percentage')
                        .message(failMessage)
                        .rejectedValue(percentage)
                        .build()
        ])

        where:
        percentage | failMessage
        null       | 'Percentage cannot be null.'
        0          | 'Percentage must be between 1 and 100.'
        -1         | 'Percentage must be between 1 and 100.'
        101        | 'Percentage must be between 1 and 100.'
    }

    def "should fail jar creation when planId is null"() {
        given: 'a new valid command'
        def command = newSampleCreateJarCommand()

        when: 'attempting to create a jar with a null plan id'
        def either = service.create(null, command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates plan not found failure'
        assert either.getLeft() == Failure.ofNotFound(PLAN_NOT_FOUND_MESSAGE)
    }

    def "should fail jar creation when plan not found"() {
        setup: 'repository mock behavior'
        1 * plans.findByIdEagerJars(_ as Plan.PlanIdentifier) >> Optional.empty()

        when: 'attempting to create a jar with an non-existing plan'
        def either = service.create(UUID.randomUUID(), newSampleCreateJarCommand())

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates plan not found failure'
        assert either.getLeft() == Failure.ofNotFound(PLAN_NOT_FOUND_MESSAGE)
    }

    def "should fail plan creation when jar name for provided name already exists"() {
        setup: 'repository mock behavior'
        1 * plans.findByIdEagerJars(_ as Plan.PlanIdentifier) >> Optional.of(newSamplePlan())
        1 * jars.existsByNameAndPlan(_ as String, _ as Plan) >> TRUE

        when: 'attempting to create a jar with an existing jar name'
        def either = service.create(UUID.randomUUID(), newSampleCreateJarCommand())

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates jar name conflict'
        assert either.getLeft() == Failure.ofConflict(JAR_NAME_DUPLICATE_MESSAGE.formatted("Necessities"))
    }

    def "should create a new jar successfully"() {
        setup: 'repository mock behavior'
        1 * plans.findByIdEagerJars(_ as Plan.PlanIdentifier) >> Optional.of(newSamplePlan())
        1 * jars.existsByNameAndPlan(_ as String, _ as Plan) >> FALSE

        when: 'creating a new jar'
        def either = service.create(UUID.randomUUID(), newSampleCreateJarCommand())

        then: 'jar value is present'
        assert either.isRight()

        and: 'jar is build as expected'
        assert either.get().getId() != null
        assert either.get().getCreationTimestamp() != null
        assert either.get().getName() == DataSamples.JAR_COMMAND.name
        assert either.get().getAmountToReachAsNumber() == BigDecimal.valueOf(550.0)
        assert either.get().getCurrencyCode() == 'EUR'
        assert either.get().getDescription() == DataSamples.JAR_COMMAND.description
        assert either.get().getPercentage().value() == DataSamples.JAR_COMMAND.percentage
        assert either.get().getPlan() == newSamplePlan()
    }
}
