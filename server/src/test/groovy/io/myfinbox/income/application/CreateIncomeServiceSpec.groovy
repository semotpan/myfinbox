package io.myfinbox.income.application

import io.myfinbox.income.domain.AccountIdentifier
import io.myfinbox.income.domain.Income
import io.myfinbox.income.domain.IncomeSources
import io.myfinbox.income.domain.Incomes
import io.myfinbox.shared.Failure
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.income.DataSamples.*
import static io.myfinbox.income.application.CreateIncomeService.INCOME_SOURCE_NOT_FOUND_MESSAGE
import static io.myfinbox.income.application.CreateIncomeService.VALIDATION_FAILURE_MESSAGE
import static io.myfinbox.income.domain.IncomeSource.IncomeSourceIdentifier

@Tag("unit")
class CreateIncomeServiceSpec extends Specification {

    IncomeSources incomeSources
    Incomes incomes
    CreateIncomeService service

    def setup() {
        incomes = Mock()
        incomeSources = Mock()
        service = new CreateIncomeService(incomeSources, incomes)
    }

    def "should fail income creation when accountId is null"() {
        given: 'a new income command with null accountId'
        def command = newValidIncomeCommand(accountId: null)

        when: 'attempting to create an income with a null accountId'
        def either = service.create(command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for create income request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('accountId')
                        .message('AccountId cannot be null.')
                        .build()
        ])
    }

    def "should fail income creation when incomeSourceId is null"() {
        given: 'a new income command with null incomeSourceId'
        def command = newValidIncomeCommand(incomeSourceId: null)

        when: 'attempting to create an income with a null categoryId'
        def either = service.create(command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for create income request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('incomeSourceId')
                        .message('IncomeSourceId cannot be null.')
                        .build()
        ])
    }

    def "should fail income creation when paymentType is invalid"() {
        given: 'a new command with an invalid payment type'
        def command = newValidIncomeCommand(paymentType: paymentType)

        when: 'attempting to create an income with an invalid paymentType'
        def either = service.create(command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'validation failure on paymentType field'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('paymentType')
                        .message(failMessage)
                        .rejectedValue(paymentType)
                        .build()
        ])

        where:
        paymentType | failMessage
        null        | "PaymentType must be 'Cash' or 'Card'."
        '  '        | "PaymentType must be 'Cash' or 'Card'."
        'Hola'      | "PaymentType must be 'Cash' or 'Card'."
    }

    def "should fail income creation when amount is invalid"() {
        given: 'a new command with an invalid amount'
        def command = newValidIncomeCommand(amount: value)

        when: 'attempting to create an income with an invalid amount'
        def either = service.create(command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'validation failure on amount field'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
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

    def "should fail income creation when currencyCode is invalid"() {
        given: 'a new command with an invalid currencyCode'
        def command = newValidIncomeCommand(currencyCode: currencyCode)

        when: 'attempting to create an income with an invalid currencyCode'
        def either = service.create(command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'validation failure on currencyCode field'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
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

    def "should fail income creation when incomeDate is null"() {
        given: 'a new command with a null incomeDate'
        def command = newValidIncomeCommand(incomeDate: null)

        when: 'attempting to create an income with a null incomeDate'
        def either = service.create(command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'validation failure on incomeDate field'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('incomeDate')
                        .message('IncomeDate cannot be null.')
                        .build()
        ])
    }

    def "should fail income creation when income source is not found"() {
        setup: 'repository mock behavior'
        1 * incomeSources.findByIdAndAccount(_ as IncomeSourceIdentifier, _ as AccountIdentifier) >> Optional.empty()

        when: 'attempting to create an income with a non-existing income source'
        def either = service.create(newValidIncomeCommand())

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'not found failure for the provided income source is available'
        assert either.getLeft() == Failure.ofNotFound(INCOME_SOURCE_NOT_FOUND_MESSAGE)
    }

    def "should create an income"() {
        setup: 'repository mock behavior and interaction'
        1 * incomeSources.findByIdAndAccount(_ as IncomeSourceIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleIncomeSource())

        when: 'creating a new income'
        def either = service.create(newValidIncomeCommand())

        then: 'income value is present'
        assert either.isRight()

        and: 'income is built as expected'
        assert either.get() == newSampleIncome([
                id               : [id: either.get().getId().toString()],
                creationTimestamp: either.get().getCreationTimestamp().toString(),
        ])

        and: 'income is saved in the repository'
        1 * incomes.save(_ as Income)
    }
}
