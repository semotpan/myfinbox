package io.myfinbox.income.application

import io.myfinbox.income.domain.Income
import io.myfinbox.income.domain.Incomes
import io.myfinbox.shared.Failure
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.income.DataSamples.newSampleIncome

@Tag("unit")
class DeleteIncomeServiceSpec extends Specification {

    Incomes incomes
    DeleteIncomeService service

    def setup() {
        incomes = Mock()
        service = new DeleteIncomeService(incomes)
    }

    def "should fail delete when income id is null"() {
        when: 'income failed to delete'
        def either = service.delete(null)

        then: 'failure result is present'
        assert either.isLeft()

        and: 'not found failure for provided incomeId'
        assert either.getLeft() == Failure.ofNotFound('Income was not found.')
    }

    def "should fail delete when income not found"() {
        setup: 'repository mock behavior and interaction'
        1 * incomes.findById(_ as Income.IncomeIdentifier) >> Optional.empty()

        when: 'income failed to delete'
        def either = service.delete(UUID.randomUUID())

        then: 'failure result is present'
        assert either.isLeft()

        and: 'not found failure for provided incomeId'
        assert either.getLeft() == Failure.ofNotFound('Income was not found.')
    }

    def "should delete an income"() {
        setup: 'repository mock behavior and interaction'
        def income = newSampleIncome()
        1 * incomes.findById(_ as Income.IncomeIdentifier) >> Optional.of(income)

        when: 'income is deleted'
        def either = service.delete(UUID.randomUUID())

        then: 'no result is present'
        assert either.isRight()

        and: 'expenses repository invoked'
        1 * incomes.delete(income)
    }
}
