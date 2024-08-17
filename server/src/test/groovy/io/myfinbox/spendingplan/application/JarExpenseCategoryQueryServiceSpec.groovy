package io.myfinbox.spendingplan.application

import io.myfinbox.spendingplan.domain.JarExpenseCategories
import io.myfinbox.spendingplan.domain.JarIdentifier
import io.myfinbox.spendingplan.domain.Jars
import io.myfinbox.spendingplan.domain.Plan
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.spendingplan.DataSamples.newSampleJarExpenseCategory

@Tag("unit")
class JarExpenseCategoryQueryServiceSpec extends Specification {

    JarExpenseCategories jarExpenseCategories
    Jars jars
    JarExpenseCategoryQueryService service

    def setup() {
        jarExpenseCategories = Mock()
        jars = Mock()
        service = new JarExpenseCategoryQueryService(jarExpenseCategories, jars)
    }

    def "should get empty list when planId is not provided"() {
        when: 'no planId or jarId is provided in the search query'
        def expenseCategories = service.search(null, null)

        then: 'the service returns an empty list of expense categories'
        assert expenseCategories.isEmpty()
    }

    def "should get empty list when jarId is not provided"() {
        when: 'no jarId is provided in the search query'
        def expenseCategories = service.search(UUID.randomUUID(), null)

        then: 'the service returns an empty list of expense categories'
        assert expenseCategories.isEmpty()
    }

    def "should get empty list when jarId for provided planId is not found"() {
        setup: 'stub the jars repository to return a non existing Jar for provided plan'
        1 * jars.existsByIdAndPlan(_ as Plan.PlanIdentifier, _ as JarIdentifier) >> Boolean.FALSE

        when: 'no planId or jarId is provided in the search query'
        def expenseCategories = service.search(UUID.randomUUID(), UUID.randomUUID())

        then: 'the service returns an empty list of expense categories'
        assert expenseCategories.isEmpty()
    }

    def "should get a list with one jar expense category"() {
        setup: 'stub the jars repository to return an existing Jar for provided plan'
        1 * jars.existsByIdAndPlan(_ as Plan.PlanIdentifier, _ as JarIdentifier) >> Boolean.TRUE
        1 * jarExpenseCategories.findByJarId(_ as JarIdentifier) >> [newSampleJarExpenseCategory()]


        when: 'no planId or jarId is provided in the search query'
        def expenseCategories = service.search(UUID.randomUUID(), UUID.randomUUID())

        then: 'the service returns a list with one expense category'
        assert expenseCategories.size() == 1
    }
}
