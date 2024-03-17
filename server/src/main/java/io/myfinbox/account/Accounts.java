package io.myfinbox.account;

import io.myfinbox.account.Account.AccountIdentifier;
import io.myfinbox.account.Account.EmailAddress;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing accounts.
 */
@Repository
public interface Accounts extends CrudRepository<Account, AccountIdentifier> {

    boolean existsByEmailAddress(EmailAddress emailAddress);

}
