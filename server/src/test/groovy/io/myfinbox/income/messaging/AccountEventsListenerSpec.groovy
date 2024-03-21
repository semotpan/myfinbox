package io.myfinbox.income.messaging

import io.myfinbox.TestServerApplication
import io.myfinbox.account.Account
import io.myfinbox.account.AccountCreated
import io.myfinbox.income.AccountIdentifier
import io.myfinbox.income.DefaultIncomeSources
import io.myfinbox.income.IncomeSource
import io.myfinbox.income.IncomeSources
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.modulith.test.ApplicationModuleTest
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification
import spock.lang.Tag

import java.time.Duration

import static io.myfinbox.income.DataSamples.newSampleDefaultSources
import static java.util.UUID.randomUUID
import static org.awaitility.Awaitility.await
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.intersect
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@Tag("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestServerApplication)
@ApplicationModuleTest
@Import(Config)
@TestPropertySource(locations = "/application-test.properties")
class AccountEventsListenerSpec extends Specification {

    static accountId = randomUUID()

    @Autowired
    IncomeSources incomeSources

    @Autowired
    Runnable eventProducer

    def "should create default income sources on account created event"() {
        when: 'account created event is published'
        eventProducer.run()

        def actual = []
        await().atMost(Duration.ofSeconds(5))
                .until {
                    actual = incomeSources.findByAccount(new AccountIdentifier(accountId))
                    !actual.isEmpty()
                }

        then: 'default income sources are created'
        def comp = { a, b -> a.name <=> b.name ?: a.account.id() <=> b.account.id() } as Comparator<? super IncomeSource>
        assert intersect(actual, newSampleDefaultSources(new AccountIdentifier(accountId)), comp).size() == DefaultIncomeSources.values().size()
    }


    @TestConfiguration
    static class Config {

        @Bean
        Runnable eventProducer(ApplicationEventPublisher eventPublisher) {
            return new Runnable() {

                @Override
                @Transactional
                void run() {
                    eventPublisher.publishEvent(new AccountCreated(
                            new Account.AccountIdentifier(accountId),
                            new Account.EmailAddress("email@email.com"),
                            "Jon",
                            "Snow"
                    ))
                }
            }
        }
    }
}
