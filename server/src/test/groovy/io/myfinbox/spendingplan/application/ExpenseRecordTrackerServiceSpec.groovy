package io.myfinbox.spendingplan.application

import io.myfinbox.shared.PaymentType
import io.myfinbox.spendingplan.domain.CategoryIdentifier
import io.myfinbox.spendingplan.domain.ExpenseRecords
import io.myfinbox.spendingplan.domain.JarExpenseCategories
import org.javamoney.moneta.Money
import spock.lang.Specification
import spock.lang.Tag

import java.time.LocalDate

import static io.myfinbox.spendingplan.DataSamples.*
import static io.myfinbox.spendingplan.domain.ExpenseRecord.ExpenseIdentifier

@Tag("unit")
class ExpenseRecordTrackerServiceSpec extends Specification {

    JarExpenseCategories jarExpenseCategories
    ExpenseRecords expenseRecords
    ExpenseRecordTrackerService service

    def setup() {
        jarExpenseCategories = Mock()
        expenseRecords = Mock()
        service = new ExpenseRecordTrackerService(jarExpenseCategories, expenseRecords)
    }

    def "should retrieve empty records when no category is tracked for a newly created expense record"() {
        given: ' the method call to return an empty list'
        1 * jarExpenseCategories.findByCategoryId(_ as CategoryIdentifier) >> []

        when: 'attempting to record the newly created expense'
        def records = service.recordCreated(newSampleExpenseModificationRecord())

        then: 'no records should be persisted'
        assert records.isEmpty()
    }

    def "should create two expense records when category is tracked"() {
        given: 'two jar categories'
        1 * jarExpenseCategories.findByCategoryId(_ as CategoryIdentifier) >> [
                newSampleJarExpenseCategory(),
                newSampleJarExpenseCategory(id: 2L)
        ]

        when: 'attempting to record expense'
        def records = service.recordCreated(newSampleExpenseModificationRecord())

        then: 'two expense records should be created'
        assert records.size() == 2

        and: 'records are constructed correctly'
        // both records are same, check only jars
        assert records.getFirst().getExpenseId() == new ExpenseIdentifier(UUID.fromString(expenseId))
        assert records.getFirst().getCategoryId() == new CategoryIdentifier(UUID.fromString(jarCategoryId))
        assert records.getFirst().getAmount() == Money.of(amount, currency)
        assert records.getFirst().getExpenseDate() == LocalDate.parse(expenseDate)
        assert records.getFirst().getPaymentType() == PaymentType.CASH
        assert records.getFirst().getJarExpenseCategory() == newSampleJarExpenseCategory()
        assert records.getFirst().getCreationTimestamp() != null

        assert records.get(1).getJarExpenseCategory() == newSampleJarExpenseCategory(id: 2L)
        assert records.get(1).getCreationTimestamp() != null


        and: 'ensure repository interaction'
        1 * expenseRecords.saveAll(_ as List)
    }

    def "should return empty records when updating an expense with no existing records"() {
        given: 'no existing expense records'
        expenseRecords.findByExpenseId(_ as ExpenseIdentifier) >> []

        when: 'attempting to update an expense record'
        def records = service.recordUpdated(newSampleExpenseModificationRecord())

        then: 'no new records should be updated'
        assert records.isEmpty()
    }

    def "should update two expense records when category is tracked"() {
        given: 'existing expense records'
        1 * expenseRecords.findByExpenseId(_ as ExpenseIdentifier) >> [
                newSampleExpenseRecord(),
                newSampleExpenseRecord(id: 2L)
        ]

        when: 'attempting to update expense records'
        def records = service.recordUpdated(newSampleExpenseModificationRecord(
                amount: [
                        amount  : 10.0,
                        currency: 'MDL'
                ],
                paymentType: 'CARD',
                expenseDate: '2024-03-22'
        ))

        then: 'two expense records should be updated'
        assert records.size() == 2

        and: 'records are updated with the expected values'
        assert records.first().amount == Money.of(10, 'MDL')
        assert records.first().expenseDate == LocalDate.parse('2024-03-22')
        assert records.first().paymentType == PaymentType.CARD

        assert records[1].amount == Money.of(10, 'MDL')
        assert records[1].expenseDate == LocalDate.parse('2024-03-22')
        assert records[1].paymentType == PaymentType.CARD

        and: 'ensure repository interaction'
        1 * expenseRecords.saveAll(_ as List)
    }

    def "should create two new expense records when category is changed, with each record linked to the new category"() {
        given: 'existing expense records and new category'
        2 * expenseRecords.findByExpenseId(_ as ExpenseIdentifier) >> [
                newSampleExpenseRecord(),
                newSampleExpenseRecord(id: 2L)
        ]

        1 * jarExpenseCategories.findByCategoryId(_ as CategoryIdentifier) >> [
                newSampleJarExpenseCategory(categoryId: [id: jarCategoryId2]),
                newSampleJarExpenseCategory(id: 2L, categoryId: [id: jarCategoryId2])
        ]

        when: 'attempting to update expense records with a new category'
        def records = service.recordUpdated(newSampleExpenseModificationRecord(
                categoryId: jarCategoryId2,
                amount: [
                        amount  : 10.0,
                        currency: 'MDL'
                ],
                paymentType: 'CARD',
                expenseDate: '2024-03-22'
        ))

        then: 'two new expense records should be created'
        assert records.size() == 2

        and: 'records are updated as expected'
        assert records.first().categoryId == new CategoryIdentifier(UUID.fromString(jarCategoryId2))
        assert records.first().amount == Money.of(10, 'MDL')
        assert records.first().expenseDate == LocalDate.parse('2024-03-22')
        assert records.first().paymentType == PaymentType.CARD
        assert records.first().jarExpenseCategory == newSampleJarExpenseCategory(categoryId: [id: jarCategoryId2])

        assert records[1].categoryId == new CategoryIdentifier(UUID.fromString(jarCategoryId2))
        assert records[1].amount == Money.of(10, 'MDL')
        assert records[1].expenseDate == LocalDate.parse('2024-03-22')
        assert records[1].paymentType == PaymentType.CARD
        assert records[1].jarExpenseCategory == newSampleJarExpenseCategory(id: 2L, categoryId: [id: jarCategoryId2])

        and: 'ensure deleted records'
        1 * expenseRecords.deleteAll(_ as List)

        and: 'ensure repository interaction'
        1 * expenseRecords.saveAll(_ as List)
    }

    def "should create one new expense record when category is changed, linked to the new category"() {
        given: 'existing expense records and a new category'
        2 * expenseRecords.findByExpenseId(_ as ExpenseIdentifier) >> [
                newSampleExpenseRecord(),
                newSampleExpenseRecord(id: 2L)
        ]

        1 * jarExpenseCategories.findByCategoryId(_ as CategoryIdentifier) >> [
                newSampleJarExpenseCategory(categoryId: [id: jarCategoryId2])
        ]

        when: 'attempting to update the expense record with a new category'
        def records = service.recordUpdated(newSampleExpenseModificationRecord(
                categoryId: jarCategoryId2,
                amount: [
                        amount  : 10.0,
                        currency: 'MDL'
                ],
                paymentType: 'CARD',
                expenseDate: '2024-03-22'
        ))

        then: 'one new expense record should be created'
        assert records.size() == 1

        and: 'the record is updated as expected'
        assert records.first().categoryId == new CategoryIdentifier(UUID.fromString(jarCategoryId2))
        assert records.first().amount == Money.of(10, 'MDL')
        assert records.first().expenseDate == LocalDate.parse('2024-03-22')
        assert records.first().paymentType == PaymentType.CARD
        assert records.first().jarExpenseCategory == newSampleJarExpenseCategory(categoryId: [id: jarCategoryId2])

        and: 'ensure deleted records'
        1 * expenseRecords.deleteAll(_ as List)

        and: 'ensure repository interaction'
        1 * expenseRecords.saveAll(_ as List)
    }

    def "should return empty records when attempting to delete a non-existent expense"() {
        given: 'no existing expense records'
        expenseRecords.findByExpenseId(_ as ExpenseIdentifier) >> []

        when: 'attempting to delete an expense record'
        def records = service.recordDeleted(newSampleExpenseModificationRecord())

        then: 'no records should be deleted'
        assert records.isEmpty()
    }

    def "should delete one record when deleting an existing expense"() {
        given: 'an existing expense record'
        expenseRecords.findByExpenseId(_ as ExpenseIdentifier) >> [
                newSampleExpenseRecord()
        ]

        when: 'attempting to delete an expense record'
        def records = service.recordDeleted(newSampleExpenseModificationRecord())

        then: 'one expense record should be deleted'
        assert records.size() == 1

        and: 'ensure the deleted record'
        1 * expenseRecords.deleteAll(_ as List)
    }
}
