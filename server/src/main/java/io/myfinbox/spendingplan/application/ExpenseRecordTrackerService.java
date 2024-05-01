package io.myfinbox.spendingplan.application;

import io.myfinbox.spendingplan.domain.CategoryIdentifier;
import io.myfinbox.spendingplan.domain.ExpenseRecord;
import io.myfinbox.spendingplan.domain.ExpenseRecords;
import io.myfinbox.spendingplan.domain.JarExpenseCategories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.myfinbox.spendingplan.domain.ExpenseRecord.ExpenseIdentifier;

/**
 * Service class for tracking expense records.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
class ExpenseRecordTrackerService implements ExpenseRecordTrackerUseCase {

    private final JarExpenseCategories jarExpenseCategories;
    private final ExpenseRecords expenseRecords;

    @Override
    public List<ExpenseRecord> recordCreated(ExpenseModificationRecord createdRecord) {
        // check if category tracked
        var expenseCategories = jarExpenseCategories.findByCategoryId(new CategoryIdentifier(createdRecord.categoryId()));
        if (expenseCategories.isEmpty()) { // skip untracked
            return List.of();
        }

        var records = expenseCategories.stream()
                .map(category -> ExpenseRecord.builder()
                        .expenseId(new ExpenseIdentifier(createdRecord.expenseId()))
                        .categoryId(new CategoryIdentifier(createdRecord.categoryId()))
                        .amount(createdRecord.amount())
                        .paymentType(createdRecord.paymentType())
                        .expenseDate(createdRecord.expenseDate())
                        .jarExpenseCategory(category)
                        .build())
                .toList();

        expenseRecords.saveAll(records);
        log.debug("Expense records {} were created", records);

        return records;
    }

    @Override
    public List<ExpenseRecord> recordUpdated(ExpenseModificationRecord updatedRecord) {
        var records = expenseRecords.findByExpenseId(new ExpenseIdentifier(updatedRecord.expenseId()));
        if (records.isEmpty()) {
            return List.of();
        }

        // check if category was changed
        if (!records.getFirst().match(new CategoryIdentifier(updatedRecord.categoryId()))) {
            log.debug("No expense records for update, expense category changed, try creating");
            recordDeleted(updatedRecord); // delete all existing expense with existing category
            return recordCreated(updatedRecord); // try creating if new category tracked
        }

        var updated = records.stream()
                .peek(record -> record.update(ExpenseRecord.builder()
                        .amount(updatedRecord.amount())
                        .paymentType(updatedRecord.paymentType())
                        .expenseDate(updatedRecord.expenseDate())))
                .toList();

        expenseRecords.saveAll(updated); // FIXME fix save anti-pattern
        log.debug("Expense records {} were updated", updated);

        return updated;
    }

    @Override
    public List<ExpenseRecord> recordDeleted(ExpenseModificationRecord deleteRecord) {
        var records = expenseRecords.findByExpenseId(new ExpenseIdentifier(deleteRecord.expenseId()));
        if (records.isEmpty()) {
            return List.of();
        }

        expenseRecords.deleteAll(records);

        log.debug("Expense records {} were deleted", records);

        return records;
    }
}
