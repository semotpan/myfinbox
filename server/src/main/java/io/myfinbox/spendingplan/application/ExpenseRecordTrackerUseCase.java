package io.myfinbox.spendingplan.application;

import io.myfinbox.shared.PaymentType;
import io.myfinbox.spendingplan.domain.ExpenseRecord;
import lombok.Builder;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static io.myfinbox.shared.Guards.notNull;

/**
 * Use case interface for tracking created, updated, and deleted expense records.
 */
public interface ExpenseRecordTrackerUseCase {

    /**
     * Records the creation of an expense.
     *
     * @param createdRecord The record of the created expense.
     * @return A list of recorded expense records.
     */
    List<ExpenseRecord> recordCreated(ExpenseModificationRecord createdRecord);

    /**
     * Records the update of an expense.
     *
     * @param updatedRecord The record of the updated expense.
     * @return A list of updated expense records.
     */
    List<ExpenseRecord> recordUpdated(ExpenseModificationRecord updatedRecord);

    /**
     * Records the deletion of an expense.
     *
     * @param deletedRecord The record of the deleted expense.
     * @return A list of deleted expense records.
     */
    List<ExpenseRecord> recordDeleted(ExpenseModificationRecord deletedRecord);

    /**
     * Record representing an expense modification (creation, update, or deletion).
     */
    @Builder
    record ExpenseModificationRecord(UUID expenseId,
                                     UUID accountId,
                                     UUID categoryId,
                                     MonetaryAmount amount,
                                     LocalDate expenseDate,
                                     PaymentType paymentType) {
        public ExpenseModificationRecord {
            // Validate non-null fields
            notNull(expenseId, "expenseId cannot be null.");
            notNull(accountId, "accountId cannot be null.");
            notNull(categoryId, "categoryId cannot be null.");
            notNull(amount, "amount cannot be null.");
            notNull(expenseDate, "expenseDate cannot be null.");
            notNull(paymentType, "paymentType cannot be null.");
        }
    }
}
