package io.myfinbox.expense

import io.myfinbox.shared.Failure
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.expense.Category.CategoryIdentifier
import static io.myfinbox.expense.CreateExpenseService.VALIDATION_FAILURE_MESSAGE
import static io.myfinbox.expense.DataSamples.*

@Tag("unit")
class CreateExpenseServiceSpec extends Specification {

    Categories categories
    Expenses expenses
    CreateExpenseService service

    def setup() {
        categories = Mock()
        expenses = Mock()
        service = new CreateExpenseService(categories, expenses)
    }

    def "should fail expense creation when accountId is null"() {
        given: 'new command with null accountId'
        def command = newSampleExpenseCommand(accountId: null)

        when: 'expense fails to create'
        def either = service.create(command)

        then: 'failure result is present'
        assert either.isLeft()

        and: 'validation failure on accountId field'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('accountId')
                        .message('AccountId cannot be null.')
                        .build()
        ])
    }

    def "should fail expense creation when categoryId is null"() {
        given: 'new command with null categoryId'
        def command = newSampleExpenseCommand(categoryId: null)

        when: 'expense fails to create'
        def either = service.create(command)

        then: 'failure result is present'
        assert either.isLeft()

        and: 'validation failure on categoryId field'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('categoryId')
                        .message('CategoryId cannot be null.')
                        .build()
        ])
    }

    def "should fail expense creation when paymentType is invalid"() {
        given: 'new command with invalid payment type'
        def command = newSampleExpenseCommand(paymentType: paymentType)

        when: 'expense fails to create'
        def either = service.create(command)

        then: 'failure result is present'
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

    def "should fail expense creation when amount is invalid"() {
        given: 'new command with invalid amount'
        def command = newSampleExpenseCommand(amount: value)

        when: 'expense fails to create'
        def either = service.create(command)

        then: 'result is present'
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
        0.0    | 'Amount must be positive value.'
        -25.56 | 'Amount must be positive value.'
    }

    def "should fail expense creation when currencyCode is invalid"() {
        given: 'new command with invalid currencyCode'
        def command = newSampleExpenseCommand(currencyCode: currencyCode)

        when: 'expense fails to create'
        def either = service.create(command)

        then: 'failure result is present'
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

    def "should fail expense creation when expenseDate is null"() {
        given: 'new command with null expenseDate'
        def command = newSampleExpenseCommand(expenseDate: null)

        when: 'expense fails to create'
        def either = service.create(command)

        then: 'failure result is present'
        assert either.isLeft()

        and: 'validation failure on expenseDate field'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('expenseDate')
                        .message('ExpenseDate cannot be null.')
                        .build()
        ])
    }

    def "should fail expense creation when category is not found"() {
        setup: 'repository mock behavior'
        1 * categories.findByIdAndAccount(_ as CategoryIdentifier, _ as AccountIdentifier) >> Optional.empty()

        when: 'expense fails to create'
        def either = service.create(newSampleExpenseCommand())

        then: 'failure result is present'
        assert either.isLeft()

        and: 'not found failure for provided category is available'
        assert either.getLeft() == Failure.ofNotFound('Category for the provided account was not found.')
    }

    def "should create an expense"() {
        setup: 'repository mock behavior and interaction'
        1 * categories.findByIdAndAccount(_ as CategoryIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleCategory())

        when: 'new expense is created'
        def either = service.create(newSampleExpenseCommand())

        then: 'expense value is present'
        assert either.isRight()

        and: 'expense is build as expected'
        assert either.get() == newSampleExpense([
                id               : [id: either.get().getId().toString()],
                creationTimestamp: either.get().getCreationTimestamp().toString(),
        ])

        and: 'expenses interaction was done'
        1 * expenses.save(_ as Expense)
    }
}
