package io.myfinbox.spendingplan

import com.fasterxml.jackson.databind.json.JsonMapper
import groovy.json.JsonOutput
import io.myfinbox.spendingplan.application.PlanCommand
import io.myfinbox.spendingplan.domain.Plan

class DataSamples {

    static MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build()

    static planId = "3b257779-a5db-4e87-9365-72c6f8d4977d"
    static accountId = "e2709aa2-7907-4f78-98b6-0f36a0c1b5ca"
    static timestamp = "2024-03-23T10:00:04.224870Z"
    static amount = 1000.0
    static currency = 'EUR'
    static name = 'My basic plan'

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

    static newSampleCretePlanResource(map = [:]) {
        JsonOutput.toJson(CREATE_PLAN_RESOURCE + map) as String
    }

    static newSampleCreatePlanCommand(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(PLAN_COMMAND + map) as String, PlanCommand.class)
    }

    static newSamplePlan(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(PLAN + map) as String, Plan.class)
    }
}
