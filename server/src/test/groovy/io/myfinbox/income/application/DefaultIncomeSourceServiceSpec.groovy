package io.myfinbox.income.application

import io.myfinbox.income.domain.AccountIdentifier
import io.myfinbox.income.domain.DefaultIncomeSources
import io.myfinbox.income.domain.IncomeSource
import io.myfinbox.income.domain.IncomeSources
import io.myfinbox.shared.Failure
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.income.DataSamples.newSampleDefaultSources
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asCollection
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.intersect

@Tag("unit")
class DefaultIncomeSourceServiceSpec extends Specification {

    IncomeSources incomeSources
    IncomeSourceService service

    def setup() {
        incomeSources = Mock()
        service = new DefaultIncomeSourceService(incomeSources)
    }

    def "should fail create default income sources when account is null"() {
        when: 'account is null'
        def either = service.createDefault(null)

        then: 'validation failure is present'
        assert either.isLeft()

        and: 'failure message is account cannot be null'
        assert either.getLeft() == Failure.ofValidation("AccountIdentifier cannot be null", List.of())
    }

    def "should create income sources"() {
        def comp = { a, b -> a.name <=> b.name ?: a.account.id() <=> b.account.id() } as Comparator<? super IncomeSource>

        setup: 'repository mock behavior and interaction'
        def account = new AccountIdentifier(UUID.randomUUID())
        1 * incomeSources.saveAll { actual ->
            intersect(asCollection(actual), newSampleDefaultSources(account), comp).size() == DefaultIncomeSources.values().size()
        } >> []

        expect: 'created default income sources for provided accountId'
        service.createDefault(account)
    }
}
