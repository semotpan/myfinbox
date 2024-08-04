package io.myfinbox.account.domain;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.regex.Pattern;

import static io.myfinbox.shared.Guards.*;

@Embeddable
public record EmailAddress(String emailAddress) implements Serializable {

    static final String patternRFC5322 = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    static final Pattern pattern = Pattern.compile(patternRFC5322);
    static final int MAX_LENGTH = 255;

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
