package io.myfinbox.spendingplan.adapter.messaging

import io.myfinbox.TestServerApplication
import io.myfinbox.shared.DomainEvent
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
import static io.myfinbox.spendingplan.domain.ExpenseRecord.ExpenseIdentifier
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

    static DomainEvent payload

    def cleanup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, 'jar_expense_record', 'spending_jar_expense_category', 'spending_jars', 'spending_plans')
    }

    @Sql('/spendingplan/messaging/create-complete-plan-structure.sql')
    def "should record received expense created event"() {
        when: 'an expense created event is published'
        payload = newSampleExpenseCreatedEvent()
        eventProducer.run()

        then: 'an expense record should exist within 5 seconds'
        List<ExpenseRecord> actual = []
        await().atMost(Duration.ofSeconds(5))
                .until {
                    actual = expenseRecords.findAll()
                    actual.size() == 1
                }

        and: 'the recorded expense matches the event payload'
        def recordedExpense = actual.first()
        assert recordedExpense.expenseId == new ExpenseIdentifier(UUID.fromString(expenseId))
        assert recordedExpense.categoryId == new CategoryIdentifier(UUID.fromString(jarCategoryId))
        assert recordedExpense.amount == Money.of(amount, "EUR")
        assert recordedExpense.paymentType == PaymentType.CASH
        assert recordedExpense.expenseDate == LocalDate.parse(expenseDate)
        assert recordedExpense.categoryName == categoryName
    }

    @Sql(['/spendingplan/messaging/create-complete-plan-structure.sql', '/spendingplan/messaging/create-expense-records.sql'])
    def "should record received expense updated event"() {
        when: 'an expense updated event is published'
        payload = newSampleExpenseUpdatedEvent(
                paymentType: "CARD",
                amount: [
                        amount  : 10,
                        currency: 'MDL'
                ],
                expenseDate: '2024-03-22',
        )
        eventProducer.run()

        then: 'an updated expense record should exist within 5 seconds'
        List<ExpenseRecord> actual = []
        await().atMost(Duration.ofSeconds(5))
                .until {
                    actual = expenseRecords.findByExpenseIdAndCategoryId(
                            new ExpenseIdentifier(UUID.fromString(expenseId)), new CategoryIdentifier(UUID.fromString(jarCategoryId)))
                    actual.size() == 1
                }

        and: 'the recorded expense matches the updated event payload'
        def recordedExpense = actual.first()
        assert recordedExpense.amount == Money.of(10, "MDL")
        assert recordedExpense.paymentType == PaymentType.CARD
        assert recordedExpense.expenseDate == LocalDate.parse('2024-03-22')
    }

    @Sql(['/spendingplan/messaging/create-complete-plan-structure.sql', '/spendingplan/messaging/create-expense-records.sql'])
    def "should record received expense updated event when category changed"() {
        when: 'an expense updated event with category change is published'
        payload = newSampleExpenseUpdatedEvent(
                expenseId: '6bd32beb-5f79-409a-8c50-ecbd3593dc12',
                categoryId: '8a366e74-b4e3-4e64-a2a6-dce273ce332a',
                paymentType: "CARD",
                amount: [
                        amount  : 10,
                        currency: 'MDL'
                ],
                expenseDate: '2024-03-22',
        )
        eventProducer.run()

        then: 'an updated expense record with the new category should exist within 5 seconds'
        List<ExpenseRecord> actual = []
        await().atMost(Duration.ofSeconds(5))
                .until {
                    actual = expenseRecords.findByExpenseIdAndCategoryId(
                            new ExpenseIdentifier(UUID.fromString('6bd32beb-5f79-409a-8c50-ecbd3593dc12')), new CategoryIdentifier(UUID.fromString('8a366e74-b4e3-4e64-a2a6-dce273ce332a')))
                    actual.size() == 1
                }

        and: 'the recorded expense matches the updated event payload'
        def recordedExpense = actual.first()
        assert recordedExpense.amount == Money.of(10, "MDL")
        assert recordedExpense.paymentType == PaymentType.CARD
        assert recordedExpense.expenseDate == LocalDate.parse('2024-03-22')
    }

    @Sql(['/spendingplan/messaging/create-complete-plan-structure.sql', '/spendingplan/messaging/create-expense-records.sql'])
    def "should record received expense deleted event when category"() {
        when: 'an expense deleted event is published'
        payload = newSampleExpenseDeletedEvent()
        eventProducer.run()

        then: 'the expense record should no longer exist within 5 seconds'
        List<ExpenseRecord> actual = []
        await().atMost(Duration.ofSeconds(5))
                .until {
                    actual = expenseRecords.findByExpenseIdAndCategoryId(
                            new ExpenseIdentifier(UUID.fromString(expenseId)), new CategoryIdentifier(UUID.fromString(jarCategoryId)))
                    actual.size() == 0
                }
    }

    @TestConfiguration
    static class Config {

        @Bean
        Runnable eventProducer(ApplicationEventPublisher eventPublisher) {
            return new Runnable() {

                @Override
                @Transactional
                void run() {
                    eventPublisher.publishEvent(payload)
                }
            }
        }
    }
}
