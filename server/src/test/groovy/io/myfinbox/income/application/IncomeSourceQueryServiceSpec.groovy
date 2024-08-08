package io.myfinbox.income.application

import io.myfinbox.income.domain.AccountIdentifier
import io.myfinbox.income.domain.IncomeSources
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.income.DataSamples.*

@Tag("unit")
class IncomeSourceQueryServiceSpec extends Specification {

    IncomeSources incomeSources
    IncomeSourceQueryService service

    def setup() {
        incomeSources = Mock()
        service = new IncomeSourceQueryService(incomeSources)
    }

    def "should get empty list when income sources for provided account id not found"() {
        setup: 'mock the repository to return an empty list for any account identifier'
        1 * incomeSources.findByAccount(_ as AccountIdentifier) >> []

        when: 'searching for income sources with a non-exiting account ID'
        def incomeSourcesList = service.search(UUID.randomUUID())

        then: 'the result should be an empty list'
        assert incomeSourcesList.isEmpty()
    }

    def "should get empty list when account id is null"() {
        when: 'searching for income sources with a null account ID'
        def incomeSourcesList = service.search(null)

        then: 'the result should be an empty list'
        assert incomeSourcesList.isEmpty()

        and: 'the repository should not be queried'
        0 * incomeSources.findByAccount(_ as AccountIdentifier)
    }

    def "should get a list of income sources"() {
        setup: 'mock the repository to return an empty list for any account identifier'
        1 * incomeSources.findByAccount(_ as AccountIdentifier) >> [newSampleIncomeSource()]

        when: 'searching for income sources with a random account ID'
        def incomeSourcesList = service.search(UUID.randomUUID())

        then: 'the result should be a list with one income source'
        assert incomeSourcesList.containsAll(newSampleIncomeSource())
    }
}
