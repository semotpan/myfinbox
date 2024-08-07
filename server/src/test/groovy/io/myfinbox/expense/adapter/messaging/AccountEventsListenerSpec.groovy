package io.myfinbox.expense.adapter.messaging

import io.myfinbox.TestServerApplication
import io.myfinbox.account.AccountCreated
import io.myfinbox.expense.domain.AccountIdentifier
import io.myfinbox.expense.domain.Categories
import io.myfinbox.expense.domain.Category
import io.myfinbox.expense.domain.DefaultCategories
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
import java.time.ZoneId

import static io.myfinbox.expense.DataSamples.newSampleDefaultCategories
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
    Categories categories

    @Autowired
    Runnable eventProducer

    def "should create default expense categories on account created event"() {
        when: 'account created event is published'
        eventProducer.run()

        def actual = []
        await().atMost(Duration.ofSeconds(5))
                .until {
                    actual = categories.findByAccount(new AccountIdentifier(accountId))
                    !actual.isEmpty()
                }

        then: 'default expense categories are created'
        def comp = { a, b -> a.name <=> b.name ?: a.account.id() <=> b.account.id() } as Comparator<? super Category>
        assert intersect(actual, newSampleDefaultCategories(new AccountIdentifier(accountId)), comp).size() == DefaultCategories.values().size()
    }


    @TestConfiguration
    static class Config {

        @Bean
        Runnable eventProducer(ApplicationEventPublisher eventPublisher) {
            return new Runnable() {

                @Override
                @Transactional
                void run() {
                    eventPublisher.publishEvent(AccountCreated.builder()
                            .accountId(accountId)
                            .emailAddress("email@email.com")
                            .firstName("Jon")
                            .lastName("Snow")
                            .currency(Currency.getInstance("MDL"))
                            .zoneId(ZoneId.of("Europe/Chisinau"))
                            .build())
                }
            }
        }
    }
}
