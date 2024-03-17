package io.myfinbox.shared;

import java.time.Instant;

public interface DomainEvent {

    default Instant issuedOn() {
        return Instant.now();
    }
}
