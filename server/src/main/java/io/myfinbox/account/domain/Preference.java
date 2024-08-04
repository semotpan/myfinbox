package io.myfinbox.account.domain;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.Currency;

import static io.myfinbox.shared.Guards.notNull;
import static lombok.AccessLevel.PACKAGE;

@ToString
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = PACKAGE, force = true)
public final class Preference implements Serializable {

    private final String currency;
    private final String zoneId;

    public Preference(Currency currency, ZoneId zoneId) {
        notNull(currency, "currency cannot be null.");
        notNull(zoneId, "zoneId cannot be null.");
        this.currency = currency.getCurrencyCode();
        this.zoneId = zoneId.getId();
    }

    public Currency currency() {
        return Currency.getInstance(currency);
    }

    public ZoneId zoneId() {
        return ZoneId.of(zoneId);
    }
}
