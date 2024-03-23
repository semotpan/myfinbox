package io.myfinbox.expense

import com.fasterxml.jackson.databind.json.JsonMapper
import groovy.json.JsonOutput

class DataSamples {

    static MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build()


    static entityId = "3b257779-a5db-4e87-9365-72c6f8d4977d"
    static accountId = "e2709aa2-7907-4f78-98b6-0f36a0c1b5ca"
    static categoryId = "3b257779-a5db-4e87-9365-72c6f8d4977d"
    static timestamp = "2024-03-23T10:00:04.224870Z"
    static expenseDate = "2023-10-13"
    static amount = 10.0
    static currency = 'EUR'

    static AMOUNT = [
            amount  : amount,
            currency: currency
    ]

    static CATEGORY = [
            id               : [id: categoryId],
            account          : [id: accountId],
            creationTimestamp: timestamp,
            name             : "Fun"
    ]

    static EXPENSE = [
            id               : [id: entityId],
            account          : [id: accountId],
            creationTimestamp: timestamp,
            category         : CATEGORY,
            amount           : AMOUNT,
            paymentType      : "CASH",
            expenseDate      : expenseDate,
            description      : "Books buying",
    ]

    static EXPENSE_COMMAND = [
            accountId   : accountId,
            categoryId  : categoryId,
            paymentType : "Cash",
            amount      : amount,
            currencyCode: currency,
            expenseDate : expenseDate,
            description : "Books buying",
    ]

    static EXPENSE_RESOURCE = [
            accountId   : accountId,
            categoryId  : categoryId,
            paymentType : "Cash",
            amount      : amount,
            currencyCode: currency,
            expenseDate : expenseDate,
            description : "Books buying",
    ]

    static EXPENSE_CREATED_EVENT = [
            expenseId  : entityId,
            accountId  : accountId,
            categoryId : categoryId,
            paymentType: "CASH",
            amount     : AMOUNT,
            expenseDate: expenseDate,
    ]

    static newSampleDefaultCategories(AccountIdentifier account) {
        DefaultCategories.asList().stream()
                .map { c -> new Category(c, account) }
                .toList()
    }

    static newSampleExpense(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(EXPENSE + map) as String, Expense.class)
    }

    static newSampleCategory(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(CATEGORY + map) as String, Category.class)
    }

    static newSampleExpenseCommand(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(EXPENSE_COMMAND + map) as String, ExpenseCommand.class)
    }

    static newValidExpenseResource(map = [:]) {
        JsonOutput.toJson(EXPENSE_RESOURCE + map) as String
    }

    static newSampleExpenseCreatedEvent(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(EXPENSE_CREATED_EVENT + map) as String, ExpenseCreated.class)
    }
}
