package io.myfinbox.spendingplan.application

import io.myfinbox.shared.Failure
import io.myfinbox.spendingplan.domain.AccountIdentifier
import io.myfinbox.spendingplan.domain.Plan
import io.myfinbox.spendingplan.domain.Plans
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.spendingplan.DataSamples.*
import static io.myfinbox.spendingplan.application.CreatePlanService.PLAN_NAME_DUPLICATE_MESSAGE
import static io.myfinbox.spendingplan.application.CreatePlanService.VALIDATION_CREATE_FAILURE_MESSAGE
import static java.lang.Boolean.FALSE
import static java.lang.Boolean.TRUE
import static org.apache.commons.lang3.RandomStringUtils.random

@Tag("unit")
class CreatePlanServiceSpec extends Specification {

    Plans plans
    CreatePlanService service

    def setup() {
        plans = Mock()
        service = new CreatePlanService(plans)
    }

    def "should fail plan creation when accountId is null"() {
        given: 'a new income command with null accountId'
        def command = newSampleCreatePlanCommand(accountId: null)

        when: 'attempting to create a plan with a null accountId'
        def either = service.create(command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for create plan request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_CREATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('accountId')
                        .message('AccountId cannot be null.')
                        .build()
        ])
    }

    def "should fail plan creation when name is invalid"() {
        given: 'a new command with an invalid name'
        def command = newSampleCreatePlanCommand(name: planName)

        when: 'attempting to create a plan with a invalid name'
        def either = service.create(command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for create plan request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_CREATE_FAILURE_MESSAGE, [
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


    def "should fail plan creation when amount is invalid"() {
        given: 'a new command with an invalid amount'
        def command = newSampleCreatePlanCommand(amount: value)

        when: 'attempting to create a plan with an invalid amount'
        def either = service.create(command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'validation failure on amount field'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_CREATE_FAILURE_MESSAGE, [
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

    def "should fail plan creation when currencyCode is invalid"() {
        given: 'a new command with an invalid currencyCode'
        def command = newSampleCreatePlanCommand(currencyCode: currencyCode)

        when: 'attempting to create a plan with an invalid currencyCode'
        def either = service.create(command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'validation failure on currencyCode field'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_CREATE_FAILURE_MESSAGE, [
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

    def "should fail plan creation when plan name already exit"() {
        setup: 'repository mock behavior'
        1 * plans.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> TRUE

        when: 'attempting to create a plan with an existing name for provided account'
        def either = service.create(newSampleCreatePlanCommand())

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'conflict failure for the provided plan name is available'
        assert either.getLeft() == Failure.ofConflict(PLAN_NAME_DUPLICATE_MESSAGE.formatted(name))
    }

    def "should create a new plan"() {
        setup: 'repository mock behavior'
        1 * plans.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> FALSE

        when: 'creating a new plan'
        def either = service.create(newSampleCreatePlanCommand())

        then: 'plan value is present'
        assert either.isRight()

        and: 'plan is build as expected'
        assert either.get() == newSamplePlan(
                id: [id: either.get().getId().toString()],
                creationTimestamp: either.get().getCreationTimestamp().toString(),
        )
    }

    static randStr(int len) {
        random(len, true, true)
    }
}
