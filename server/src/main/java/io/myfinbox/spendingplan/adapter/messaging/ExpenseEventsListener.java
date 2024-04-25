package io.myfinbox.spendingplan.adapter.messaging;

import io.myfinbox.expense.ExpenseCreated;
import io.myfinbox.spendingplan.application.ExpenseRecordTrackerUseCase;
import io.myfinbox.spendingplan.application.ExpenseRecordTrackerUseCase.ExpenseCreatedRecord;
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
        log.debug("Received ExpenseCreated event: {}", event);

        // Record the created expense
        var expenseRecord = expenseRecordTrackerUseCase.recordCreated(ExpenseCreatedRecord.builder()
                .expenseId(event.expenseId())
                .accountId(event.accountId())
                .categoryId(event.categoryId())
                .amount(event.amount())
                .paymentType(event.paymentType())
                .expenseDate(event.expenseDate())
                .build());

        if (expenseRecord.isEmpty()) {
            log.debug("ExpenseCreated event: {} skipped", event);
        }
    }
}
