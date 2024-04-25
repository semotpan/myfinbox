package io.myfinbox.spendingplan.application

import io.myfinbox.spendingplan.domain.CategoryIdentifier
import io.myfinbox.spendingplan.domain.ExpenseRecords
import io.myfinbox.spendingplan.domain.JarExpenseCategories
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.spendingplan.DataSamples.newSampleExpenseCreateRecord
import static io.myfinbox.spendingplan.DataSamples.newSampleExpenseRecord
import static io.myfinbox.spendingplan.DataSamples.newSampleJarExpenseCategory

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

    def "should get empty records when tracking categories not tracked"() {
        given: 'a new expense record'
        def createRecord = newSampleExpenseCreateRecord()
        1 * jarExpenseCategories.findByCategoryId(_ as CategoryIdentifier) >> []

        when: 'attempting to record expense'
        def records = service.recordCreated(createRecord)

        then: 'no records were persisted'
        assert records.isEmpty()
    }

    def "should get records when tracking categories tracked"() {
        given: 'a new expense record'
        def createRecord = newSampleExpenseCreateRecord()
        1 * jarExpenseCategories.findByCategoryId(_ as CategoryIdentifier) >> [
                newSampleJarExpenseCategory()
        ]

        when: 'attempting to record expense'
        def records = service.recordCreated(createRecord)

        then: 'records were persisted'
        assert records.first() == newSampleExpenseRecord(
                id: records.first().getId(),
                creationTimestamp: records.first().getCreationTimestamp().toString(),
                jarExpenseCategory: newSampleJarExpenseCategory()
        )

        and: ''
        1 * expenseRecords.saveAll(_ as List)
    }
}
