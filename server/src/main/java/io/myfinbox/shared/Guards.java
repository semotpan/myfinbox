package io.myfinbox.shared;

import lombok.Generated;
import org.apache.commons.lang3.StringUtils;
import org.javamoney.moneta.Money;
import org.springframework.util.CollectionUtils;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import static java.math.BigDecimal.ZERO;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Generated // exclude from jacoco
public final class Guards {

    private Guards() {
    }

    public static <T> T notNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }

        return object;
    }

    public static String notBlank(String text, String message) {
        if (isBlank(text)) {
            throw new IllegalArgumentException(message);
        }

        return text;
    }

    public static BigDecimal greaterThanZero(BigDecimal value, String message) {
        notNull(value, "value cannot be null.");
        if (value.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    public static MonetaryAmount greaterThanZero(MonetaryAmount amount, String message) {
        notNull(amount, "amount cannot be null.");
        if (amount.isLessThanOrEqualTo(Money.of(BigDecimal.ZERO, amount.getCurrency()))) {
            throw new IllegalArgumentException(message);
        }

        return amount;
    }

    public static String doesNotOverflow(String text, int maxLength, String message) {
        if (StringUtils.length(text) > maxLength) {
            throw new IllegalArgumentException(message);
        }

        return text;
    }

    public static String matches(String value, Pattern pattern, String message) {
        notNull(value, "value cannot be null.");
        notNull(pattern, "pattern cannot be null.");
        if (!pattern.matcher(value).matches()) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }

    public static List<?> nonEmpty(List<?> values, String message) {
        if (CollectionUtils.isEmpty(values)) {
            throw new IllegalArgumentException(message);
        }

        return values;
    }
}
