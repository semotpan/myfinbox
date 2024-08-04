package io.myfinbox.account.domain;

import io.myfinbox.account.AccountCreated;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

import static io.myfinbox.shared.Guards.notNull;
import static lombok.AccessLevel.PRIVATE;

@Entity
@Table(name = "accounts")
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PRIVATE, force = true)
public class Account extends AbstractAggregateRoot<Account> {

    public static final int MAX_LENGTH = 255;

    public static final String patternRFC5322 = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    static final Pattern pattern = Pattern.compile(patternRFC5322);

    @EmbeddedId
    private final AccountIdentifier id;
    private final Instant creationDate;

    @Embedded
    private EmailAddress emailAddress;

    @Embedded
    private AccountDetails accountDetails;

    @Embedded
    private Preference preference;

    @Builder
    public Account(AccountDetails accountDetails,
                   EmailAddress emailAddress,
                   Preference preference) {
        this.accountDetails = notNull(accountDetails, "accountDetails cannot be null");
        this.emailAddress = notNull(emailAddress, "emailAddress cannot be null");
        this.preference = notNull(preference, "preference cannot be null");

        this.id = new AccountIdentifier(UUID.randomUUID());
        this.creationDate = Instant.now();

        registerEvent(AccountCreated.builder()
                .accountId(this.id.id())
                .emailAddress(this.emailAddress.emailAddress())
                .firstName(this.accountDetails.firstName())
                .lastName(this.accountDetails.lastName())
                .currency(this.preference.currency())
                .zoneId(this.preference.zoneId())
                .build());
    }

    @Embeddable
    public record AccountIdentifier(UUID id) implements Serializable {

        public AccountIdentifier {
            notNull(id, "id cannot be null");
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }
}
