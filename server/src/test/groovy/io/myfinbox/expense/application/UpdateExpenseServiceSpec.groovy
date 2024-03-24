package io.myfinbox.expense.application

import io.myfinbox.expense.domain.AccountIdentifier
import io.myfinbox.expense.domain.Categories
import io.myfinbox.expense.domain.Category
import io.myfinbox.expense.domain.Expenses
import io.myfinbox.shared.Failure
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.expense.DataSamples.*
import static io.myfinbox.expense.application.UpdateExpenseService.*
import static io.myfinbox.expense.domain.Expense.ExpenseIdentifier

@Tag("unit")
class UpdateExpenseServiceSpec extends Specification {

    Categories categories
    Expenses expenses
    UpdateExpenseService service

    def setup() {
        categories = Mock()
        expenses = Mock()
        service = new UpdateExpenseService(categories, expenses)
    }

    def "should fail expense updating when accountId is null"() {
        given: 'a new expense command with null accountId'
        def command = newSampleExpenseCommand(accountId: null)

        when: 'attempting to update an expense with a null accountId'
        def either = service.update(UUID.randomUUID(), command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for update expense request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('accountId')
                        .message('AccountId cannot be null.')
                        .build()
        ])
    }

    def "should fail expense updating when categoryId is null"() {
        given: 'a new expense command with null categoryId'
        def command = newSampleExpenseCommand(categoryId: null)

        when: 'attempting to update an expense with a null categoryId'
        def either = service.update(UUID.randomUUID(), command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for update expense request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('categoryId')
                        .message('CategoryId cannot be null.')
                        .build()
        ])
    }

    def "should fail expense updating when paymentType is invalid"() {
        given: 'a new command with an invalid payment type'
        def command = newSampleExpenseCommand(paymentType: paymentType)

        when: 'attempting to update an expense with an invalid paymentType'
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

    def "should fail expense updating when amount is invalid"() {
        given: 'a new command with an invalid amount'
        def command = newSampleExpenseCommand(amount: value)

        when: 'attempting to update an expense with an invalid amount'
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

    def "should fail expense updating when currencyCode is invalid"() {
        given: 'a new command with an invalid currencyCode'
        def command = newSampleExpenseCommand(currencyCode: currencyCode)

        when: 'attempting to update an expense with an invalid currencyCode'
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

    def "should fail expense updating when expenseDate is null"() {
        given: 'a new command with a null expenseDate'
        def command = newSampleExpenseCommand(expenseDate: null)

        when: 'attempting to update an expense with a null expenseDate'
        def either = service.update(UUID.randomUUID(), command)

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

    def "should fail expense updating when expense ID is null"() {
        when: 'attempting to update an expense with a non-existing expense'
        def either = service.update(null, newSampleExpenseCommand())

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'not found failure for the provided expense is available'
        assert either.getLeft() == Failure.ofNotFound(EXPENSE_NOT_FOUND_MESSAGE)
    }

    def "should fail expense updating when expense is not found"() {
        setup: 'a non-existing expense in the database'
        1 * expenses.findByIdAndAccount(_ as ExpenseIdentifier, _ as AccountIdentifier) >> Optional.empty()

        when: 'attempting to update an expense with a non-existing expense'
        def either = service.update(UUID.randomUUID(), newSampleExpenseCommand())

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'not found failure for the provided expense is available'
        assert either.getLeft() == Failure.ofNotFound(EXPENSE_NOT_FOUND_MESSAGE)
    }

    def "should fail expense updating when expense category is not found"() {
        setup: 'a non-existing category in the database'
        1 * expenses.findByIdAndAccount(_ as ExpenseIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleExpense())
        1 * categories.findByIdAndAccount(_ as Category.CategoryIdentifier, _ as AccountIdentifier) >> Optional.empty()

        when: 'attempting to update an expense with a non-existing category'
        def either = service.update(UUID.randomUUID(), newSampleExpenseCommand(categoryId: UUID.randomUUID()))

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'not found failure for the provided category is available'
        assert either.getLeft() == Failure.ofNotFound(CATEGORY_NOT_FOUND_MESSAGE)
    }

    def "should update an expense"() {
        setup: 'an existing expense in the database'
        1 * expenses.findByIdAndAccount(_ as ExpenseIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleExpense())
        1 * categories.findByIdAndAccount(_ as Category.CategoryIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleCategory())

        when: 'updating an expense'
        def either = service.update(UUID.randomUUID(), newSampleExpenseCommand(
                categoryId: UUID.randomUUID(),
                paymentType: "Card",
                amount: 50.0,
                currencyCode: "USD",
                expenseDate: "2024-03-24",
                description: "Other expenses"
        ))

        then: 'updated expense value is present'
        assert either.isRight()

        and: 'updated expense is built as expected'
        assert either.get() == newSampleExpense(
                paymentType: "CARD",
                amount: [
                        amount  : 50.0,
                        currency: "USD"
                ],
                expenseDate: "2024-03-24",
                description: "Other expenses",
        )
    }
}
