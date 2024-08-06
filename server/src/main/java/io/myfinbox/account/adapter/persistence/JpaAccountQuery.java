package io.myfinbox.account.adapter.persistence;

import io.myfinbox.account.application.AccountQuery;
import io.myfinbox.account.domain.Account;
import io.myfinbox.account.domain.Accounts;
import io.myfinbox.shared.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static io.myfinbox.account.domain.Account.AccountIdentifier;
import static io.myfinbox.shared.Failure.ofNotFound;
import static java.util.Objects.isNull;

@Repository
@RequiredArgsConstructor
class JpaAccountQuery implements AccountQuery {

    static final String ACCOUNT_NOT_FOUND = "Account '%s' not found.";

    private final Accounts accounts;

    @Override
    @Transactional(readOnly = true)
    public Either<Failure, Account> findOne(UUID accountId) {
        if (isNull(accountId)) {
            return Either.left(ofNotFound(ACCOUNT_NOT_FOUND.formatted(accountId)));
        }

        return accounts.findById(new AccountIdentifier(accountId))
                .<Either<Failure, Account>>map(Either::right)
                .orElseGet(() -> Either.left(ofNotFound(ACCOUNT_NOT_FOUND.formatted(accountId))));
    }
}
