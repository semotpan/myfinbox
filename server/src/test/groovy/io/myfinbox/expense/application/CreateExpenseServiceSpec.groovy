package io.myfinbox.expense.application

import io.myfinbox.expense.domain.AccountIdentifier
import io.myfinbox.expense.domain.Categories
import io.myfinbox.expense.domain.Expense
import io.myfinbox.expense.domain.Expenses
import io.myfinbox.shared.Failure
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.expense.DataSamples.*
import static io.myfinbox.expense.application.CreateExpenseService.CATEGORY_NOT_FOUND_MESSAGE
import static io.myfinbox.expense.application.CreateExpenseService.VALIDATION_FAILURE_MESSAGE
import static io.myfinbox.expense.domain.Category.CategoryIdentifier

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
        given: 'a new expense command with null accountId'
        def command = newSampleExpenseCommand(accountId: null)

        when: 'attempting to create an expense with a null accountId'
        def either = service.create(command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for create expense request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('accountId')
                        .message('AccountId cannot be null.')
                        .build()
        ])
    }

    def "should fail expense creation when categoryId is null"() {
        given: 'a new expense command with null categoryId'
        def command = newSampleExpenseCommand(categoryId: null)

        when: 'attempting to create an expense with a null categoryId'
        def either = service.create(command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for create expense request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('categoryId')
                        .message('CategoryId cannot be null.')
                        .build()
        ])
    }

    def "should fail expense creation when paymentType is invalid"() {
        given: 'a new command with an invalid payment type'
        def command = newSampleExpenseCommand(paymentType: paymentType)

        when: 'attempting to create an expense with an invalid paymentType'
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

    def "should fail expense creation when amount is invalid"() {
        given: 'a new command with an invalid amount'
        def command = newSampleExpenseCommand(amount: value)

        when: 'attempting to create an expense with an invalid amount'
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

    def "should fail expense creation when currencyCode is invalid"() {
        given: 'a new command with an invalid currencyCode'
        def command = newSampleExpenseCommand(currencyCode: currencyCode)

        when: 'attempting to create an expense with an invalid currencyCode'
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

    def "should fail expense creation when expenseDate is null"() {
        given: 'a new command with a null expenseDate'
        def command = newSampleExpenseCommand(expenseDate: null)

        when: 'attempting to create an expense with a null expenseDate'
        def either = service.create(command)

        then: 'a failure result is present'
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

        when: 'attempting to create an expense with a non-existing category'
        def either = service.create(newSampleExpenseCommand())

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'not found failure for the provided category is available'
        assert either.getLeft() == Failure.ofNotFound(CATEGORY_NOT_FOUND_MESSAGE)
    }

    def "should create an expense"() {
        setup: 'repository mock behavior and interaction'
        1 * categories.findByIdAndAccount(_ as CategoryIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleCategory())

        when: 'creating a new expense'
        def either = service.create(newSampleExpenseCommand())

        then: 'expense value is present'
        assert either.isRight()

        and: 'expense is built as expected'
        assert either.get() == newSampleExpense([
                id               : [id: either.get().getId().toString()],
                creationTimestamp: either.get().getCreationTimestamp().toString(),
        ])

        and: 'expense is saved in the repository'
        1 * expenses.save(_ as Expense)
    }
}
