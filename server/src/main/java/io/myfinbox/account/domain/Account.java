package io.myfinbox.account.domain;

import io.myfinbox.account.AccountCreated;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

import static io.myfinbox.shared.Guards.*;
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

    private @EmbeddedId AccountIdentifier id;
    private @Embedded EmailAddress emailAddress;
    private String firstName;
    private String lastName;
    private Instant creationDate;

    @Builder
    public Account(String firstName, String lastName, EmailAddress emailAddress) {
        this.emailAddress = notNull(emailAddress, "emailAddress cannot be null");

        if (!StringUtils.isBlank(firstName)) {
            this.firstName = doesNotOverflow(firstName.trim(), MAX_LENGTH, "firstName overflow, max length allowed '%d'".formatted(MAX_LENGTH));
        }

        if (!StringUtils.isBlank(lastName)) {
            this.lastName = doesNotOverflow(lastName.trim(), MAX_LENGTH, "lastName overflow, max length allowed '%d'".formatted(MAX_LENGTH));
        }

        this.id = new AccountIdentifier(UUID.randomUUID());
        this.creationDate = Instant.now();

        registerEvent(new AccountCreated(this.id.id(), this.emailAddress.emailAddress(), firstName, lastName));
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

    @Embeddable
    public record EmailAddress(String emailAddress) implements Serializable {

        public EmailAddress {
            notBlank(emailAddress, "emailAddress cannot be blank");
            doesNotOverflow(emailAddress.trim(), MAX_LENGTH, "emailAddress max length must be '%d'".formatted(MAX_LENGTH));
            matches(emailAddress, pattern, "emailAddress must match '%s'".formatted(patternRFC5322));
        }

        @Override
        public String toString() {
            return emailAddress;
        }
    }
}
