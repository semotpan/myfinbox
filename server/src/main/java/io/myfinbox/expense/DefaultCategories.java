package io.myfinbox.expense;

import java.util.Arrays;
import java.util.List;

/**
 * Default list of category name, used to init for each new account
 */
public enum DefaultCategories {

    FOOD("Food"),
    CAR("Transport"),
    MEDICAL("Medical"),
    EDUCATION("Education"),
    TRAVEL("Travel"),
    CLOTHING("Clothing"),
    FUN("Fun"),
    PERSONAL("Personal"),
    LOAN("Loan"),
    BUSINESS("Business"),
    OTHER("Other");

    public final String text;

    DefaultCategories(String text) {
        this.text = text;
    }

    public static List<String> asList() {
        return Arrays.stream(values())
                .map(v -> v.text)
                .toList();
    }
}
