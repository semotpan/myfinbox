package io.myfinbox.income.messaging;

import io.myfinbox.account.AccountCreated;
import io.myfinbox.income.AccountIdentifier;
import io.myfinbox.income.IncomeSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * Listens for account events and handles account creation events by initializing default income sources.
 */
@Component(value = "incomeAccountEventsListener")
@RequiredArgsConstructor
@Slf4j
class AccountEventsListener {

    private final IncomeSourceService incomeSourceService;

    /**
     * Handles account creation events by initializing default income sources.
     *
     * @param event The account creation event.
     */
    @ApplicationModuleListener
    @Transactional(propagation = REQUIRES_NEW)
    public void on(AccountCreated event) {
        log.debug("Handle account create event {}", event);
        var either = incomeSourceService.createDefault(new AccountIdentifier(event.accountIdentifier().id()));

        if (either.isLeft()) {
            log.error("Failed to create default income sources for account: {}, failure: {}", event, either.getLeft());
        }
    }
}
