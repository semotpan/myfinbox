package io.myfinbox.income

import com.fasterxml.jackson.databind.json.JsonMapper

class DataSamples {

    static MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build()

    static newSampleDefaultSources(AccountIdentifier account) {
        DefaultIncomeSources.asList().stream()
                .map { is -> new IncomeSource(is, account) }
                .toList()
    }
}
