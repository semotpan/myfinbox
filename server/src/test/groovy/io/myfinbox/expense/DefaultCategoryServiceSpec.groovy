package io.myfinbox.expense

import io.myfinbox.shared.Failure
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.expense.DataSamples.newSampleDefaultCategories
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asCollection
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.intersect

@Tag("unit")
class DefaultCategoryServiceSpec extends Specification {

    Categories categories
    CategoryService service

    def setup() {
        categories = Mock()
        service = new DefaultCategoryService(categories)
    }

    def "should fail create default categories when account is null"() {
        when: 'account is null'
        def either = service.initDefaultCategories(null)

        then: 'validation failure is present'
        assert either.isLeft()

        and: 'failure message is account cannot be null'
        assert either.getLeft() == Failure.ofValidation("AccountIdentifier cannot be null", List.of())
    }

    def "should create default categories"() {
        def comp = { a, b -> a.name <=> b.name ?: a.account.id() <=> b.account.id() } as Comparator<? super Category>

        setup: 'repository mock behavior and interaction'
        def account = new AccountIdentifier(UUID.randomUUID())
        1 * categories.saveAll { actual ->
            intersect(asCollection(actual), newSampleDefaultCategories(account), comp).size() == DefaultCategories.values().size()
        } >> []

        expect: 'created default categories for provided accountId'
        service.initDefaultCategories(account)
    }
}
