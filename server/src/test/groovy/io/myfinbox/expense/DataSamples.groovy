package io.myfinbox.expense

import com.fasterxml.jackson.databind.json.JsonMapper

class DataSamples {

    static MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build()

    static newSampleDefaultCategories(AccountIdentifier account) {
        DefaultCategories.asList().stream()
                .map { c -> new Category(c, account) }
                .toList()
    }
}
