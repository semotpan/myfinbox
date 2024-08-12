package io.myfinbox.spendingplan.application

import io.myfinbox.spendingplan.domain.AccountIdentifier
import io.myfinbox.spendingplan.domain.Plan
import io.myfinbox.spendingplan.domain.Plans
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.spendingplan.DataSamples.*

@Tag("unit")
class PlanQueryServiceSpec extends Specification {

    Plans plans
    PlanQueryService service

    def setup() {
        plans = Mock()
        service = new PlanQueryService(plans)
    }

    def "should get empty list when accountId and planId are not provided" () {
        when: 'no accountId or planId is provided in the search query'
        def plans = service.search().list()

        then: 'the service returns an empty list of plans'
        assert plans.isEmpty()
    }

    def "should get a list with one plan when accountId and planId are provided" () {
        setup: 'stub the plans repository to return a plan when both accountId and planId are provided'
        1 * plans.findByIdAndAccountIdEagerJars(_ as Plan.PlanIdentifier, _ as AccountIdentifier) >> Optional.of(newSamplePlan())

        when: 'search is performed with both accountId and planId'
        def plans = service.search()
                .withAccountId(UUID.fromString(accountId))
                .withPlanId(UUID.fromString(planId))
                .list()

        then: 'the service returns a list containing one plan'
        assert plans.size() == 1
    }

    def "should get a list with one plan when accountId is provided" () {
        setup: 'stub the plans repository to return a plan when accountId is provided'
        1 * plans.findByAccountIdEagerJars(_ as AccountIdentifier) >> [newSamplePlan()]

        when: 'search is performed with only accountId'
        def plans = service.search()
                .withAccountId(UUID.fromString(accountId))
                .list()

        then: 'the service returns a list containing one plan'
        assert plans.size() == 1
    }

    def "should get a list with one plan when planId is provided" () {
        setup: 'stub the plans repository to return a plan when planId is provided'
        1 * plans.findByIdEagerJars(_ as Plan.PlanIdentifier) >> Optional.of(newSamplePlan())

        when: 'search is performed with only planId'
        def plans = service.search()
                .withPlanId(UUID.fromString(planId))
                .list()

        then: 'the service returns a list containing one plan'
        assert plans.size() == 1
    }
}
