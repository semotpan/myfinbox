package io.myfinbox.income

import com.fasterxml.jackson.databind.json.JsonMapper
import groovy.json.JsonOutput
import io.myfinbox.income.application.IncomeCommand
import io.myfinbox.income.application.IncomeSourceService
import io.myfinbox.income.domain.AccountIdentifier
import io.myfinbox.income.domain.DefaultIncomeSources
import io.myfinbox.income.domain.Income
import io.myfinbox.income.domain.IncomeSource

class DataSamples {

    static MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build()

    static incomeId = "3b257779-a5db-4e87-9365-72c6f8d4977d"
    static accountId = "e2709aa2-7907-4f78-98b6-0f36a0c1b5ca"
    static incomeSourceId = "3b257779-a5db-4e87-9365-72c6f8d4977d"
    static incomeSourceId2 = "e2709aa2-7907-4f78-98b6-0f36a0c1b5ca"
    static timestamp = "2024-03-23T10:00:04.224870Z"
    static incomeDate = "2024-03-23"
    static amount = 10.0
    static currency = 'EUR'
    static name = 'Business'

    static AMOUNT = [
            amount  : amount,
            currency: currency
    ]

    static INCOME_SOURCE = [
            id               : [id: incomeSourceId],
            account          : [id: accountId],
            creationTimestamp: timestamp,
            name             : name
    ]

    static INCOME = [
            id               : [id: incomeId],
            account          : [id: accountId],
            creationTimestamp: timestamp,
            incomeSource     : INCOME_SOURCE,
            amount           : AMOUNT,
            paymentType      : "CASH",
            incomeDate       : incomeDate,
            description      : "Dividends income",
    ]

    static INCOME_COMMAND = [
            accountId     : accountId,
            incomeSourceId: incomeSourceId,
            paymentType   : "Cash",
            amount        : amount,
            currencyCode  : currency,
            incomeDate    : incomeDate,
            description   : "Dividends income",
    ]

    static INCOME_RESOURCE = [
            accountId     : accountId,
            incomeSourceId: incomeSourceId,
            paymentType   : "Cash",
            amount        : amount,
            currencyCode  : currency,
            incomeDate    : incomeDate,
            description   : "Dividends income",
    ]

    static INCOME_EVENT = [
            incomeId      : incomeId,
            accountId     : accountId,
            incomeSourceId: incomeSourceId,
            paymentType   : "CASH",
            amount        : AMOUNT,
            incomeDate    : incomeDate,
    ]

    static INCOME_SOURCE_RESOURCE = [
            accountId: accountId,
            name     : name,
    ]

    static newSampleDefaultSources(AccountIdentifier account) {
        DefaultIncomeSources.asList().stream()
                .map { is -> new IncomeSource(is, account) }
                .toList()
    }

    static newSampleIncome(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(INCOME + map) as String, Income.class)
    }

    static newSampleIncomeSource(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(INCOME_SOURCE + map) as String, IncomeSource.class)
    }

    static newSampleIncomeSourceCommand(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(INCOME_SOURCE_RESOURCE + map) as String, IncomeSourceService.IncomeSourceCommand.class)
    }

    static newValidIncomeSourceResource(map = [:]) {
        JsonOutput.toJson(INCOME_SOURCE_RESOURCE + map) as String
    }

    static newValidIncomeResource(map = [:]) {
        JsonOutput.toJson(INCOME_RESOURCE + map) as String
    }

    static newValidIncomeCommand(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(INCOME_COMMAND + map) as String, IncomeCommand.class)
    }

    static newValidIncomeCreatedEvent(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(INCOME_EVENT + map) as String, IncomeCreated.class)
    }

    static newValidIncomeUpdatedEvent(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(INCOME_EVENT + map) as String, IncomeUpdated.class)
    }

    static newValidIncomeDeletedEvent(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(INCOME_EVENT + map) as String, IncomeDeleted.class)
    }
}
