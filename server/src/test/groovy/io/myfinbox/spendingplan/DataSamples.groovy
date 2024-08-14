package io.myfinbox.spendingplan

import com.fasterxml.jackson.databind.json.JsonMapper
import groovy.json.JsonOutput
import io.myfinbox.expense.ExpenseCreated
import io.myfinbox.expense.ExpenseDeleted
import io.myfinbox.expense.ExpenseUpdated
import io.myfinbox.spendingplan.application.JarCommand
import io.myfinbox.spendingplan.application.PlanCommand
import io.myfinbox.spendingplan.domain.*
import org.javamoney.moneta.Money

import static io.myfinbox.spendingplan.application.AddOrRemoveJarCategoryUseCase.JarCategoriesCommand
import static io.myfinbox.spendingplan.application.ClassicPlanBuilderUseCase.CreateClassicPlanCommand
import static io.myfinbox.spendingplan.application.ExpenseRecordTrackerUseCase.ExpenseModificationRecord

class DataSamples {

    static MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build()

    static expenseId = "3b257779-a5db-4e87-9365-72c6f8d4977d"
    static planId = "3b257779-a5db-4e87-9365-72c6f8d4977d"
    static jarId = "e2709aa2-7907-4f78-98b6-0f36a0c1b5ca"
    static jarId2 = "a6993312-2e45-43e4-b965-9edc88da7a00"
    static accountId = "e2709aa2-7907-4f78-98b6-0f36a0c1b5ca"
    static jarCategoryId = "e2709aa2-7907-4f78-98b6-0f36a0c1b5ca"
    static jarCategoryId2 = "ee0a4cdc-84f0-4f81-8aea-224dad4915e7"
    static timestamp = "2024-03-23T10:00:04.224870Z"
    static amount = 1000.00
    static currency = 'EUR'
    static name = 'My basic plan'
    static jarName = 'Necessities'
    static expenseDate = "2024-03-23"
    static String jarDescription = "Necessities spending: Rent, Food, Bills etc."
    static String planDescription = "My basic plan for tracking expenses"

    static AMOUNT = [
            amount  : amount,
            currency: currency
    ]

    static CREATE_PLAN_RESOURCE = [
            name        : name,
            accountId   : accountId,
            amount      : amount,
            currencyCode: currency,
            description : planDescription,
    ]

    static CREATE_CLASSIC_PLAN_RESOURCE = [
            accountId   : accountId,
            amount      : amount,
            currencyCode: currency,
    ]

    static PLAN_COMMAND = [
            name        : name,
            accountId   : accountId,
            amount      : amount,
            currencyCode: currency,
            description : planDescription,
    ]

    static CLASSIC_PLAN_COMMAND = [
            accountId   : accountId,
            amount      : amount,
            currencyCode: currency
    ]

    static PLAN = [
            id               : [id: planId],
            name             : name,
            account          : [id: accountId],
            creationTimestamp: timestamp,
            amount           : AMOUNT,
            description      : planDescription
    ]

    static PLAN_PLAIN = [
            planId           : planId,
            name             : name,
            accountId        : accountId,
            creationTimestamp: timestamp,
            amount           : amount,
            currencyCode     : currency,
            description      : planDescription
    ]

    static CREATE_JAR_RESOURCE = [
            name       : 'Necessities',
            percentage : 55,
            description: jarDescription,
    ]

    static JAR_COMMAND = [
            name       : jarName,
            percentage : 55,
            description: jarDescription
    ]

    static JAR = [
            id               : [id: jarId],
            percentage       : [value: 55],
            amountToReach    : AMOUNT + [amount: 550],
            creationTimestamp: timestamp,
            name             : jarName,
            description      : jarDescription,
    ]

    static JAR_PLAIN = [
            jarId            : jarId,
            percentage       : 55,
            amountToReach    : 550.00,
            creationTimestamp: timestamp,
            name             : jarName,
            description      : jarDescription,
    ]


    static JAR_CATEGORIES_COMMAND = [
            categories: [JAR_CATEGORY_TO_ADD_OR_REMOVE]
    ]

    static JAR_CATEGORY_TO_ADD_OR_REMOVE = [
            categoryId: jarCategoryId,
            toAdd     : true
    ]

    static EXPENSE_MODIFICATION_RECORD = [
            expenseId  : expenseId,
            accountId  : accountId,
            categoryId : jarCategoryId,
            paymentType: "CASH",
            amount     : AMOUNT,
            expenseDate: expenseDate,
    ]

    static EXPENSE_EVENT = [
            expenseId  : expenseId,
            accountId  : accountId,
            categoryId : jarCategoryId,
            paymentType: "CASH",
            amount     : AMOUNT,
            expenseDate: expenseDate,
    ]

    static EXPENSE_RECORD = [
            id         : 1L,
            expenseId  : [id: expenseId],
            categoryId : [id: jarCategoryId],
            paymentType: "CASH",
            amount     : AMOUNT,
            expenseDate: expenseDate,
    ]

    static JAR_EXPENSE_CATEGORY = [
            id        : 1,
            categoryId: [id: jarCategoryId]
    ]

    static JAR_CATEGORIES_RESOURCE = [
            categories: []
    ]

    static newSampleCreatePlanResource(map = [:]) {
        JsonOutput.toJson(CREATE_PLAN_RESOURCE + map) as String
    }

    static newSampleClassicCreatePlanResource(map = [:]) {
        JsonOutput.toJson(CREATE_CLASSIC_PLAN_RESOURCE + map) as String
    }

    static newSampleCreatePlanCommand(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(PLAN_COMMAND + map) as String, PlanCommand.class)
    }

    static newSampleClassicCreatePlanCommand(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(CLASSIC_PLAN_COMMAND + map) as String, CreateClassicPlanCommand.class)
    }

    static newSamplePlan(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(PLAN + map) as String, Plan.class)
    }

    static newSamplePlanWithJarAsString() {
        JsonOutput.toJson(PLAN_PLAIN + [jars: [JAR_PLAIN]])
    }

    static newSampleListPlanWithJarAsString() {
        JsonOutput.toJson([PLAN_PLAIN + [jars: [JAR_PLAIN]]])
    }

    static newSamplePlanBuilder() {
        Plan.builder()
                .name(name)
                .account(new AccountIdentifier(UUID.fromString(accountId)))
                .amount(Money.of(BigDecimal.valueOf(amount), currency))
                .description(planDescription)
                .jars([newSampleJar()])
    }

    static newSampleCreateJarResource(map = [:]) {
        JsonOutput.toJson(CREATE_JAR_RESOURCE + map) as String
    }

    static newSampleCreateJarCommand(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(JAR_COMMAND + map) as String, JarCommand.class)
    }

    static newSampleJar(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(JAR + map) as String, Jar.class)
    }

    static newSampleJarCategoriesCommand(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(JAR_CATEGORIES_COMMAND + map) as String, JarCategoriesCommand.class)
    }

    static newSampleJarCategoryToAddAsMap(map = [:]) {
        return JAR_CATEGORY_TO_ADD_OR_REMOVE + map
    }

    static newSampleJarCategoriesResource(map = [:]) {
        JsonOutput.toJson(JAR_CATEGORIES_RESOURCE + map) as String
    }

    static newSampleExpenseModificationRecord(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(EXPENSE_MODIFICATION_RECORD + map) as String, ExpenseModificationRecord.class)
    }

    static newSampleJarExpenseCategory(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(JAR_EXPENSE_CATEGORY + map) as String, JarExpenseCategory.class)
    }

    static newSampleExpenseCreatedEvent(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(EXPENSE_EVENT + map) as String, ExpenseCreated.class)
    }

    static newSampleExpenseRecord(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(EXPENSE_RECORD + map) as String, ExpenseRecord.class)
    }

    static newSampleExpenseUpdatedEvent(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(EXPENSE_EVENT + map) as String, ExpenseUpdated.class)
    }

    static newSampleExpenseDeletedEvent(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(EXPENSE_EVENT + map) as String, ExpenseDeleted.class)
    }

    static newSampleJarAsString() {
        JsonOutput.toJson(JAR_PLAIN)
    }

    static newSampleListJarAsString() {
        JsonOutput.toJson([JAR_PLAIN])
    }
}
