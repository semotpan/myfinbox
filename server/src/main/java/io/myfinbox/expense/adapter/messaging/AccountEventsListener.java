package io.myfinbox.expense.adapter.messaging;

import io.myfinbox.account.AccountCreated;
import io.myfinbox.expense.application.CategoryService;
import io.myfinbox.expense.domain.AccountIdentifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Listens for account events and handles account creation events by initializing default categories.
 */
@Component(value = "expenseAccountEventsListener")
@RequiredArgsConstructor
@Slf4j
class AccountEventsListener {

    private final CategoryService categoryService;

    /**
     * Handles account creation events by initializing default categories.
     *
     * @param event The account creation event.
     */
    @ApplicationModuleListener
    public void on(AccountCreated event) {
        log.debug("Handle account create event {}", event);
        var either = categoryService.createDefault(new AccountIdentifier(event.accountId()));

        if (either.isLeft()) {
            log.error("Failed to create default categories for account: {}, failure: {}", event, either.getLeft());
        }
    }
}
