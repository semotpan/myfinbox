package io.myfinbox.income.application

import io.myfinbox.income.domain.*
import io.myfinbox.shared.Failure
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.income.DataSamples.*
import static io.myfinbox.income.application.CreateIncomeService.INCOME_SOURCE_NOT_FOUND_MESSAGE
import static io.myfinbox.income.application.UpdateIncomeService.INCOME_NOT_FOUND_MESSAGE
import static io.myfinbox.income.application.UpdateIncomeService.VALIDATION_FAILURE_MESSAGE

@Tag("unit")
class UpdateIncomeServiceSpec extends Specification {

    IncomeSources incomeSources
    Incomes incomes
    UpdateIncomeUseCase service

    def setup() {
        incomes = Mock()
        incomeSources = Mock()
        service = new UpdateIncomeService(incomeSources, incomes)
    }

    def "should fail income updating when accountId is null"() {
        given: 'a new income command with null accountId'
        def command = newValidIncomeCommand(accountId: null)

        when: 'attempting to update an income with a null accountId'
        def either = service.update(UUID.randomUUID(), command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for update income request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('accountId')
                        .message('AccountId cannot be null.')
                        .build()
        ])
    }

    def "should fail income updating when incomeSourceId is null"() {
        given: 'a new income command with null incomeSourceId'
        def command = newValidIncomeCommand(incomeSourceId: null)

        when: 'attempting to update an income with a null incomeSourceId'
        def either = service.update(UUID.randomUUID(), command)

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

    def "should fail income updating when paymentType is invalid"() {
        given: 'a new command with an invalid payment type'
        def command = newValidIncomeCommand(paymentType: paymentType)

        when: 'attempting to update an income with an invalid paymentType'
        def either = service.update(UUID.randomUUID(), command)

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

    def "should fail income updating when amount is invalid"() {
        given: 'a new command with an invalid amount'
        def command = newValidIncomeCommand(amount: value)

        when: 'attempting to create an income with an invalid amount'
        def either = service.update(UUID.randomUUID(), command)

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

    def "should fail income updating when currencyCode is invalid"() {
        given: 'a new command with an invalid currencyCode'
        def command = newValidIncomeCommand(currencyCode: currencyCode)

        when: 'attempting to update an income with an invalid currencyCode'
        def either = service.update(UUID.randomUUID(), command)

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

    def "should fail income updating when incomeDate is null"() {
        given: 'a new command with a null incomeDate'
        def command = newValidIncomeCommand(incomeDate: null)

        when: 'attempting to create an income with a null incomeDate'
        def either = service.update(UUID.randomUUID(), command)

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

    def "should fail income updating when income id null with income not found"() {
        when: 'attempting to update an income with a null income id'
        def either = service.update(null, newValidIncomeCommand())

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'not found failure for the provided income id is available'
        assert either.getLeft() == Failure.ofNotFound(INCOME_NOT_FOUND_MESSAGE)
    }


    def "should fail income updating when income is not found"() {
        setup: 'repository mock behavior'
        1 * incomes.findByIdAndAccount(_ as Income.IncomeIdentifier, _ as AccountIdentifier) >> Optional.empty()

        when: 'attempting to update an income with non-existing income'
        def either = service.update(UUID.randomUUID(), newValidIncomeCommand())

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'not found failure for the provided income is available'
        assert either.getLeft() == Failure.ofNotFound(INCOME_NOT_FOUND_MESSAGE)
    }

    def "should fail income updating when income source is not found"() {
        setup: 'repository mock behavior'
        1 * incomes.findByIdAndAccount(_ as Income.IncomeIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleIncome())
        1 * incomeSources.findByIdAndAccount(_ as IncomeSource.IncomeSourceIdentifier, _ as AccountIdentifier) >> Optional.empty()

        when: 'attempting to update an income with a non-existing income source'
        def either = service.update(UUID.randomUUID(), newValidIncomeCommand(incomeSourceId: UUID.randomUUID()))

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'not found failure for the provided income source is available'
        assert either.getLeft() == Failure.ofNotFound(INCOME_SOURCE_NOT_FOUND_MESSAGE)
    }

    def "should update an income"() {
        setup: 'repository mock behavior and interaction'
        1 * incomes.findByIdAndAccount(_ as Income.IncomeIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleIncome())
        1 * incomeSources.findByIdAndAccount(_ as IncomeSource.IncomeSourceIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleIncomeSource())

        when: 'updating a new income'
        def either = service.update(UUID.randomUUID(), newValidIncomeCommand(
                incomeSourceId: UUID.randomUUID(),
                paymentType: "Card",
                amount: 50.0,
                currencyCode: "USD",
                incomeDate: "2024-03-24",
                description: "Other expenses"
        ))

        then: 'income value is present'
        assert either.isRight()

        and: 'income is built as expected'
        assert either.get() == newSampleIncome([
                paymentType: "CARD",
                amount     : [
                        amount  : 50.0,
                        currency: "USD"
                ],
                incomeDate : "2024-03-24",
                description: "Other expenses",
        ])

        and: 'income is updated in the repository'
        1 * incomes.save(_ as Income)
    }
}
