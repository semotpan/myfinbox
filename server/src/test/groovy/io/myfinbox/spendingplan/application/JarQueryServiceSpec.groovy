package io.myfinbox.spendingplan.application

import io.myfinbox.spendingplan.domain.JarIdentifier
import io.myfinbox.spendingplan.domain.Jars
import io.myfinbox.spendingplan.domain.Plan
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.spendingplan.DataSamples.*

@Tag("unit")
class JarQueryServiceSpec extends Specification {

    Jars jars
    JarQueryService service

    def setup() {
        jars = Mock()
        service = new JarQueryService(jars)
    }

    def "should get empty list when planId and jarId are not provided"() {
        when: 'no planId or jarId is provided in the search query'
        def plans = service.search().list()

        then: 'the service returns an empty list of jars'
        assert plans.isEmpty()
    }

    def "should get a list with one jar when planId and jarId are provided"() {
        setup: 'stub the jars repository to return a jar when both planId and jarId are provided'
        1 * jars.findByIdAndPlanId(_ as JarIdentifier, _ as Plan.PlanIdentifier) >> Optional.of(newSampleJar())

        when: 'search is performed with both planId and jarId'
        def plans = service.search()
                .withPlanId(UUID.fromString(planId))
                .withJarId(UUID.fromString(jarId))
                .list()

        then: 'the service returns a list containing one jar'
        assert plans.size() == 1
    }

    def "should get a list with one jar when jarId is provided"() {
        setup: 'stub the jars repository to return a jar when jarId is provided'
        1 * jars.findById(_ as JarIdentifier) >> Optional.of(newSampleJar())

        when: 'search is performed with jarId'
        def plans = service.search()
                .withJarId(UUID.fromString(jarId))
                .list()

        then: 'the service returns a list containing one jar'
        assert plans.size() == 1
    }

    def "should get a list with one jar when planId is provided"() {
        setup: 'stub the jars repository to return a jar when jarId is provided'
        1 * jars.findByPlanId(_ as Plan.PlanIdentifier) >> [newSampleJar()]

        when: 'search is performed with planId'
        def plans = service.search()
                .withPlanId(UUID.fromString(planId))
                .list()

        then: 'the service returns a list containing one jar'
        assert plans.size() == 1
    }
}
