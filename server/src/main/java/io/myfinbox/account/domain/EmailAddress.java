package io.myfinbox.account.domain;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.regex.Pattern;

import static io.myfinbox.shared.Guards.*;

@Embeddable
public record EmailAddress(String value) implements Serializable {

    public static final String patternRFC5322 = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    static final Pattern pattern = Pattern.compile(patternRFC5322);
    public static final int MAX_LENGTH = 255;

    public EmailAddress {
        notBlank(value, "value cannot be blank");
        doesNotOverflow(value.trim(), MAX_LENGTH, "value max length must be '%d'".formatted(MAX_LENGTH));
        matches(value, pattern, "value must match '%s'".formatted(patternRFC5322));
    }

    @Override
    public String toString() {
        return value;
    }
}
