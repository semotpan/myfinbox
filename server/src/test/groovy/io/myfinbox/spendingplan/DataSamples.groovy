package io.myfinbox.spendingplan


import com.fasterxml.jackson.databind.json.JsonMapper
import groovy.json.JsonOutput
import io.myfinbox.spendingplan.application.JarCommand
import io.myfinbox.spendingplan.application.PlanCommand
import io.myfinbox.spendingplan.domain.Jar
import io.myfinbox.spendingplan.domain.Plan

class DataSamples {

    static MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build()

    static planId = "3b257779-a5db-4e87-9365-72c6f8d4977d"
    static jarId = "e2709aa2-7907-4f78-98b6-0f36a0c1b5ca"
    static accountId = "e2709aa2-7907-4f78-98b6-0f36a0c1b5ca"
    static timestamp = "2024-03-23T10:00:04.224870Z"
    static amount = 1000.0
    static currency = 'EUR'
    static name = 'My basic plan'
    static jarName = 'Necessities'

    static AMOUNT = [
            amount  : amount,
            currency: currency
    ]

    static CREATE_PLAN_RESOURCE = [
            name        : name,
            accountId   : accountId,
            amount      : amount,
            currencyCode: currency,
            description : "My basic plan for tracking expenses",
    ]

    static PLAN_COMMAND = [
            name        : name,
            accountId   : accountId,
            amount      : amount,
            currencyCode: currency,
            description : "My basic plan for tracking expenses",
    ]

    static PLAN = [
            id         : [id: planId],
            name       : name,
            account    : [id: accountId],
            amount     : AMOUNT,
            description: "My basic plan for tracking expenses",
    ]

    static CREATE_JAR_RESOURCE = [
            name       : 'Necessities',
            percentage : 55,
            description: "Necessities spending: Rent, Food, Bills etc.",
    ]

    static JAR_COMMAND = [
            name       : jarName,
            percentage : 55,
            description: "Necessities spending: Rent, Food, Bills etc."
    ]

    static JAR = [
            id           : [id: jarId],
            percentage   : [value: 55],
            amountToReach: AMOUNT + [amount: 550],
            name         : jarName,
            description  : "Necessities spending: Rent, Food, Bills etc.",
    ]

    static newSampleCreatePlanResource(map = [:]) {
        JsonOutput.toJson(CREATE_PLAN_RESOURCE + map) as String
    }

    static newSampleCreatePlanCommand(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(PLAN_COMMAND + map) as String, PlanCommand.class)
    }

    static newSamplePlan(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(PLAN + map) as String, Plan.class)
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
}
