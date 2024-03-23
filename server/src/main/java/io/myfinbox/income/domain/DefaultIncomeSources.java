package io.myfinbox.income.domain;

import java.util.Arrays;
import java.util.List;

public enum DefaultIncomeSources {

    SALARY("Salary"),
    Business("Business"),
    LOAN("Loan");

    public final String text;

    DefaultIncomeSources(String text) {
        this.text = text;
    }

    public static List<String> asList() {
        return Arrays.stream(values())
                .map(v -> v.text)
                .toList();
    }
}
