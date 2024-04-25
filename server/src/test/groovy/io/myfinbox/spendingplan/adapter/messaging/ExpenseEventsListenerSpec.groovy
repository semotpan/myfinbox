package io.myfinbox.spendingplan.adapter.messaging

import io.myfinbox.TestServerApplication
import io.myfinbox.shared.PaymentType
import io.myfinbox.spendingplan.domain.CategoryIdentifier
import io.myfinbox.spendingplan.domain.ExpenseRecord
import io.myfinbox.spendingplan.domain.ExpenseRecords
import org.javamoney.moneta.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.modulith.test.ApplicationModuleTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification
import spock.lang.Tag

import java.time.Duration
import java.time.LocalDate

import static io.myfinbox.spendingplan.DataSamples.*
import static org.awaitility.Awaitility.await
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@Tag("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestServerApplication)
@ApplicationModuleTest
@Import(Config)
@TestPropertySource(locations = "/application-test.properties")
class ExpenseEventsListenerSpec extends Specification {

    @Autowired
    Runnable eventProducer

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    ExpenseRecords expenseRecords

    def cleanup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, 'jar_expense_record', 'spending_jar_expense_category', 'spending_jars', 'spending_plans')
    }

    @Sql('/spendingplan/messaging/create-complete-plan-structure.sql')
    def "should record received expense created event"() {
        when: 'expense created event is published'
        eventProducer.run()

        then: 'wait until one expense record should exist'
        List<ExpenseRecord> actual = []
        await().atMost(Duration.ofSeconds(5))
                .until {
                    actual = expenseRecords.findAll()
                    actual.size() == 1
                }

        and: 'record is build as expected'
        assert actual.getFirst().getExpenseId() == new ExpenseRecord.ExpenseIdentifier(UUID.fromString(expenseId))
        assert actual.getFirst().getCategoryId() == new CategoryIdentifier(UUID.fromString(jarCategoryId))
        assert actual.getFirst().getAmount() == Money.of(amount, "EUR")
        assert actual.getFirst().getPaymentType() == PaymentType.CASH
        assert actual.getFirst().getExpenseDate() == LocalDate.parse(expenseDate)
    }

    @TestConfiguration
    static class Config {

        @Bean
        Runnable eventProducer(ApplicationEventPublisher eventPublisher) {
            return new Runnable() {

                @Override
                @Transactional
                void run() {
                    eventPublisher.publishEvent(newSampleExpenseCreatedEvent())
                }
            }
        }
    }
}
