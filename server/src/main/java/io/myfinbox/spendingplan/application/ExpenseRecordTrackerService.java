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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
class ExpenseRecordTrackerService implements ExpenseRecordTrackerUseCase {

    private final JarExpenseCategories jarExpenseCategories;
    private final ExpenseRecords expenseRecords;

    @Override
    public List<ExpenseRecord> recordCreated(ExpenseCreatedRecord createdRecord) {
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
}
