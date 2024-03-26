package io.myfinbox.expense.application

import io.myfinbox.expense.domain.Expense
import io.myfinbox.expense.domain.Expenses
import io.myfinbox.shared.Failure
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.expense.DataSamples.newSampleExpense

@Tag("unit")
class DeleteExpenseServiceSpec extends Specification {

    Expenses expenses
    DeleteExpenseService service

    def setup() {
        expenses = Mock()
        service = new DeleteExpenseService(expenses)
    }

    def "should fail delete when expense id is null"() {
        when: 'expense failed to delete'
        def either = service.delete(null)

        then: 'failure result is present'
        assert either.isLeft()

        and: 'not found failure for provided expenseId'
        assert either.getLeft() == Failure.ofNotFound('Expense was not found.')
    }

    def "should fail delete when expense not found"() {
        setup: 'repository mock behavior and interaction'
        1 * expenses.findById(_ as Expense.ExpenseIdentifier) >> Optional.empty()

        when: 'expense failed to delete'
        def either = service.delete(UUID.randomUUID())

        then: 'failure result is present'
        assert either.isLeft()

        and: 'not found failure for provided expenseId'
        assert either.getLeft() == Failure.ofNotFound('Expense was not found.')
    }

    def "should delete an expense"() {
        setup: 'repository mock behavior and interaction'
        def expense = newSampleExpense()
        1 * expenses.findById(_ as Expense.ExpenseIdentifier) >> Optional.of(expense)

        when: 'expense is deleted'
        def either = service.delete(UUID.randomUUID())

        then: 'no result is present'
        assert either.isRight()

        and: 'expenses repository invoked'
        1 * expenses.delete(expense)
    }
}
