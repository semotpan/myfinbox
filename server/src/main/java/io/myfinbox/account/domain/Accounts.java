package io.myfinbox.account.domain;

import io.myfinbox.account.domain.Account.AccountIdentifier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing accounts.
 */
@Repository
public interface Accounts extends CrudRepository<Account, AccountIdentifier> {

    boolean existsByEmailAddress(EmailAddress emailAddress);

}
