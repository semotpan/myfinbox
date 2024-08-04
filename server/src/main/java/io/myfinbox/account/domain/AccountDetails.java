package io.myfinbox.account.domain;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

import static io.myfinbox.shared.Guards.doesNotOverflow;
import static lombok.AccessLevel.PACKAGE;

@ToString
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = PACKAGE, force = true)
public final class AccountDetails implements Serializable {

    public static final int MAX_LENGTH = 255;

    private String firstName;
    private String lastName;

    public AccountDetails(String firstName, String lastName) {
        if (!StringUtils.isBlank(firstName)) {
            this.firstName = doesNotOverflow(firstName.trim(), MAX_LENGTH, "firstName overflow, max length allowed '%d'".formatted(MAX_LENGTH));
        }

        if (!StringUtils.isBlank(lastName)) {
            this.lastName = doesNotOverflow(lastName.trim(), MAX_LENGTH, "lastName overflow, max length allowed '%d'".formatted(MAX_LENGTH));
        }
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }
}
