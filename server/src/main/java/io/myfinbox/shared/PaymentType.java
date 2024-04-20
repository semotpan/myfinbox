package io.myfinbox.shared;

public enum PaymentType {

    CASH("Cash"),
    CARD("Card");

    private final String value;

    PaymentType(String value) {
        this.value = value;
    }

    public static boolean containsValue(String value) {
        for (var b : values()) {
            if (b.value.equalsIgnoreCase(value))
                return true;
        }

        return false;
    }

    public static PaymentType fromValue(String value) {
        for (var b : values()) {
            if (b.value.equalsIgnoreCase(value))
                return b;
        }

        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    public String value() {
        return value;
    }
}
