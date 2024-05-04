package io.myfinbox.spendingplan.application

import io.myfinbox.shared.Failure
import io.myfinbox.spendingplan.domain.AccountIdentifier
import io.myfinbox.spendingplan.domain.Plan
import io.myfinbox.spendingplan.domain.Plans
import org.javamoney.moneta.Money
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.spendingplan.DataSamples.newSampleCreatePlanCommand
import static io.myfinbox.spendingplan.DataSamples.newSamplePlan
import static io.myfinbox.spendingplan.application.UpdatePlanService.PLAN_NAME_DUPLICATE_MESSAGE
import static io.myfinbox.spendingplan.application.UpdatePlanService.VALIDATION_UPDATE_FAILURE_MESSAGE
import static java.lang.Boolean.FALSE
import static java.lang.Boolean.TRUE
import static org.apache.commons.lang3.RandomStringUtils.random

@Tag("unit")
class UpdatePlanServiceSpec extends Specification {

    Plans plans
    UpdatePlanService service

    def setup() {
        plans = Mock()
        service = new UpdatePlanService(plans)
    }

    def "should fail plan updating when accountId is null"() {
        given: 'a new plan command with null accountId'
        def command = newSampleCreatePlanCommand(accountId: null)

        when: 'attempting to update a plan with a null accountId'
        def either = service.update(UUID.randomUUID(), command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for the update plan request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_UPDATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('accountId')
                        .message('AccountId cannot be null.')
                        .build()
        ])
    }

    def "should fail plan updating when name is invalid"() {
        given: 'a new command with an invalid name'
        def command = newSampleCreatePlanCommand(name: planName)

        when: 'attempting to update a plan with an invalid name'
        def either = service.update(UUID.randomUUID(), command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for the update plan request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_UPDATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('name')
                        .message(failMessage)
                        .rejectedValue(planName)
                        .build()
        ])

        where:
        planName     | failMessage
        null         | 'Name cannot be empty.'
        ''           | 'Name cannot be empty.'
        '   '        | 'Name cannot be empty.'
        randStr(256) | "Name length cannot exceed ${Plan.MAX_NAME_LENGTH} characters."
    }

    def "should fail plan updating when amount is invalid"() {
        given: 'a new command with an invalid amount'
        def command = newSampleCreatePlanCommand(amount: value)

        when: 'attempting to update a plan with an invalid amount'
        def either = service.update(UUID.randomUUID(), command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for the update plan request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_UPDATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('amount')
                        .message(failMessage)
                        .rejectedValue(value)
                        .build()
        ])

        where:
        value  | failMessage
        null   | 'Amount cannot be null.'
        0.0    | 'Amount must be a positive value.'
        -25.56 | 'Amount must be a positive value.'
    }

    def "should fail plan updating when currency code is invalid"() {
        given: 'a new command with an invalid currency code'
        def command = newSampleCreatePlanCommand(currencyCode: currencyCode)

        when: 'attempting to update a plan with an invalid currency code'
        def either = service.update(UUID.randomUUID(), command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for the update plan request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_UPDATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('currencyCode')
                        .message(failMessage)
                        .rejectedValue(currencyCode)
                        .build()
        ])

        where:
        currencyCode | failMessage
        null         | 'CurrencyCode cannot be null.'
        ''           | 'CurrencyCode is not valid.'
        'US'         | 'CurrencyCode is not valid.'
        'MDLA'       | 'CurrencyCode is not valid.'
    }

    def "should fail plan update when plan id is null"() {
        when: 'attempting to update a plan with a null plan id'
        def either = service.update(null, newSampleCreatePlanCommand())

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates plan not found'
        assert either.getLeft() == Failure.ofNotFound(UpdatePlanService.PLAN_NOT_FOUND_MESSAGE)
    }

    def "should fail plan update when plan not found"() {
        setup: 'mock behavior for repository'
        1 * plans.findByIdEagerJars(_ as Plan.PlanIdentifier) >> Optional.empty()

        when: 'attempting to update a non-existing plan'
        def either = service.update(UUID.randomUUID(), newSampleCreatePlanCommand())

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates plan not found'
        assert either.getLeft() == Failure.ofNotFound(UpdatePlanService.PLAN_NOT_FOUND_MESSAGE)
    }

    def "should skip update when plan not changed"() {
        setup: 'mock behavior for repository'
        def existingPlan = newSamplePlan()
        1 * plans.findByIdEagerJars(_ as Plan.PlanIdentifier) >> Optional.of(existingPlan)

        when: 'attempting to update a plan with the same details'
        def either = service.update(UUID.randomUUID(), newSampleCreatePlanCommand())

        then: 'the result is successful and the plan is not updated'
        assert either.isRight()
        assert either.get() == existingPlan
    }

    def "should fail plan update when plan name already exists"() {
        setup: 'mock behavior for repository'
        def existingPlan = newSamplePlan()
        1 * plans.findByIdEagerJars(_ as Plan.PlanIdentifier) >> Optional.of(existingPlan)
        1 * plans.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> TRUE

        when: 'attempting to update a plan with an existing name for the provided account'
        def either = service.update(UUID.randomUUID(), newSampleCreatePlanCommand(name: 'Other name'))

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'conflict failure for the provided plan name is available'
        assert either.getLeft() == Failure.ofConflict(PLAN_NAME_DUPLICATE_MESSAGE.formatted('Other name'))
    }

    def "should update plan successfully"() {
        setup: 'mock behavior for repository'
        def existingPlan = newSamplePlan()
        1 * plans.findByIdEagerJars(_ as Plan.PlanIdentifier) >> Optional.of(existingPlan)
        1 * plans.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> FALSE

        when: 'attempting to update a plan with new details'
        def updatedPlanCommand = newSampleCreatePlanCommand(
                name        : 'Other name',
                amount      : 2000.0,
                currencyCode: 'MDL',
                description : 'Some Update'
        )
        def either = service.update(UUID.randomUUID(), updatedPlanCommand)

        then: 'the updated plan is returned successfully'
        assert either.isRight()

        and: 'the plan is updated with the new details'
        def updatedPlan = either.get()
        assert updatedPlan.getName() == 'Other name'
        assert updatedPlan.getAmount() == Money.of(BigDecimal.valueOf(2000), 'MDL')
        assert updatedPlan.getDescription() == 'Some Update'
    }

    static randStr(int len) {
        random(len, true, true)
    }
}
