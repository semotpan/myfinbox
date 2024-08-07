package io.myfinbox.expense.application

import io.myfinbox.expense.domain.AccountIdentifier
import io.myfinbox.expense.domain.Categories
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.expense.DataSamples.newSampleCategory

@Tag("unit")
class CategoryQueryServiceSpec extends Specification {

    Categories categories
    CategoryQueryService service

    def setup() {
        categories = Mock()
        service = new CategoryQueryService(categories)
    }

    def "should get empty list when categories for provided account id not found"() {
        setup: 'mock the repository to return an empty list for any account identifier'
        1 * categories.findByAccount(_ as AccountIdentifier) >> []

        when: 'searching for categories with a non-exiting account ID'
        def categoryList = service.search(UUID.randomUUID())

        then: 'the result should be an empty list'
        assert categoryList.isEmpty()
    }

    def "should get empty list when account id is null"() {
        when: 'searching for categories with a null account ID'
        def categoryList = service.search(null)

        then: 'the result should be an empty list'
        assert categoryList.isEmpty()

        and: 'the repository should not be queried'
        0 * categories.findByAccount(_ as AccountIdentifier)
    }

    def "should get a list of categories"() {
        setup: 'mock the repository to return a list with a sample category for any account identifier'
        1 * categories.findByAccount(_ as AccountIdentifier) >> [newSampleCategory()]

        when: 'Searching for categories with a random account ID'
        def categoryList = service.search(UUID.randomUUID())

        then: 'the result should be a list with one category'
        assert categoryList.size() == 1
    }
}
