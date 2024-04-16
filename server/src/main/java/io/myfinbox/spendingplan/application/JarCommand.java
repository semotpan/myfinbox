package io.myfinbox.spendingplan.application;

import lombok.Builder;

@Builder
public record JarCommand(String name,
                         Integer percentage,
                         String description) {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_PERCENTAGE = "percentage";

}
