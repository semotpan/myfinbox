package io.myfinbox.spendingplan.adapter.messaging;

import io.myfinbox.expense.ExpenseCreated;
import io.myfinbox.expense.ExpenseDeleted;
import io.myfinbox.expense.ExpenseUpdated;
import io.myfinbox.spendingplan.application.ExpenseRecordTrackerUseCase;
import io.myfinbox.spendingplan.application.ExpenseRecordTrackerUseCase.ExpenseModificationRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Listens for expense-related events and delegates the handling to the appropriate use case.
 */
@Slf4j
@RequiredArgsConstructor
@Component(value = "planExpenseEventsListener")
class ExpenseEventsListener {

    private final ExpenseRecordTrackerUseCase expenseRecordTrackerUseCase;

    /**
     * Handles the ExpenseCreated event by recording it.
     *
     * @param event The ExpenseCreated event to handle.
     */
    @ApplicationModuleListener
    public void on(ExpenseCreated event) {
        log.debug("[Plan] Received ExpenseCreated event: {}", event);

        // Record the created expense
        var expenseRecord = expenseRecordTrackerUseCase.recordCreated(ExpenseModificationRecord.builder()
                .expenseId(event.expenseId())
                .accountId(event.accountId())
                .categoryId(event.categoryId())
                .amount(event.amount())
                .paymentType(event.paymentType())
                .expenseDate(event.expenseDate())
                .build());

        if (expenseRecord.isEmpty()) {
            log.debug("[Plan] ExpenseCreated event: {} skipped", event);
        }
    }

    /**
     * Handles the ExpenseUpdated event by recording it.
     *
     * @param event The ExpenseUpdated event to handle.
     */
    @ApplicationModuleListener
    public void on(ExpenseUpdated event) {
        log.debug("[Plan] Received ExpenseUpdated event: {}", event);

        // Record the updated expense
        var expenseRecord = expenseRecordTrackerUseCase.recordUpdated(ExpenseModificationRecord.builder()
                .expenseId(event.expenseId())
                .accountId(event.accountId())
                .categoryId(event.categoryId())
                .amount(event.amount())
                .paymentType(event.paymentType())
                .expenseDate(event.expenseDate())
                .build());

        if (expenseRecord.isEmpty()) {
            log.debug("[Plan] ExpenseUpdated event: {} skipped", event);
        }
    }

    /**
     * Handles the ExpenseDeleted event by recording it.
     *
     * @param event The ExpenseDeleted event to handle.
     */
    @ApplicationModuleListener
    public void on(ExpenseDeleted event) {
        log.debug("[Plan] Received ExpenseDeleted event: {}", event);

        // Record the deleted expense
        var expenseRecord = expenseRecordTrackerUseCase.recordDeleted(ExpenseModificationRecord.builder()
                .expenseId(event.expenseId())
                .accountId(event.accountId())
                .categoryId(event.categoryId())
                .amount(event.amount())
                .paymentType(event.paymentType())
                .expenseDate(event.expenseDate())
                .build());

        if (expenseRecord.isEmpty()) {
            log.debug("[Plan] ExpenseDeleted event: {} skipped", event);
        }
    }
}
