package io.myfinbox.account;

import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.regex.Pattern.compile;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Entity
@Table(name = "accounts")
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = PRIVATE, force = true)
public class Account extends AbstractAggregateRoot<Account> {

    static final String patternRFC5322 = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    static final int MAX_LENGTH = 255;

    private @EmbeddedId AccountIdentifier id;
    private @Embedded EmailAddress emailAddress;
    private String firstName;
    private String lastName;
    private Instant creationDate;

    @Builder
    public Account(String firstName, String lastName, EmailAddress emailAddress) {
        this.emailAddress = requireNonNull(emailAddress, "emailAddress cannot be null");

        if (!StringUtils.isBlank(firstName)) {
            requireNonOverflow(firstName, "firstName overflow, max length allowed '%d'".formatted(MAX_LENGTH));
            this.firstName = firstName.trim();
        }

        if (!StringUtils.isBlank(lastName)) {
            requireNonOverflow(lastName, "lastName overflow, max length allowed '%d'".formatted(MAX_LENGTH));
            this.lastName = lastName.trim();
        }

        this.id = new AccountIdentifier(UUID.randomUUID());
        this.creationDate = Instant.now();

        registerEvent(new AccountCreated(this.id, this.emailAddress, firstName, lastName));
    }

    private static void requireNonOverflow(String text, String message) {
        if (StringUtils.length(text) > Account.MAX_LENGTH)
            throw new IllegalArgumentException(message);
    }

    @Embeddable
    public record AccountIdentifier(UUID id) implements Serializable {

        public AccountIdentifier {
            requireNonNull(id, "id cannot be null");
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }

    @Embeddable
    public record EmailAddress(String emailAddress) implements Serializable {

        public EmailAddress {
            if (isBlank(emailAddress)) {
                throw new IllegalArgumentException("emailAddress cannot be blank");
            }

            requireNonOverflow(emailAddress, "emailAddress max length must be '%d'".formatted(MAX_LENGTH));

            if (!compile(patternRFC5322).matcher(emailAddress).matches()) {
                throw new IllegalArgumentException("emailAddress must match '%s'".formatted(patternRFC5322));
            }
        }

        @Override
        public String toString() {
            return emailAddress;
        }
    }
}
