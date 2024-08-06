package io.myfinbox.account

import com.fasterxml.jackson.databind.json.JsonMapper
import groovy.json.JsonOutput

import static io.myfinbox.account.application.CreateAccountUseCase.CreateAccountCommand

class DataSamples {

    static MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build()

    static ACCOUNT_RESOURCE = [
            accountId        : "e2709aa2-7907-4f78-98b6-0f36a0c1b5ca",
            firstName        : "Jon",
            lastName         : "Snow",
            emailAddress     : "jonsnow@gmail.com",
            zoneId           : "Europe/Chisinau",
            currency         : "MDL",
            creationTimestamp: "2024-03-23T10:00:04.224870Z"
    ]

    static ACCOUNT_CREATE_COMMAND = [
            firstName   : "Jon",
            lastName    : "Snow",
            emailAddress: "jonsnow@gmail.com",
            zoneId      : "Europe/Chisinau",
            currency    : "MDL"
    ]

    static ACCOUNT_CREATED_EVENT = [
            accountId   : "e2709aa2-7907-4f78-98b6-0f36a0c1b5ca",
            emailAddress: "jonsnow@gmail.com",
            firstName   : "Jon",
            lastName    : "Snow",
            currency    : "MDL",
            zoneId      : "Europe/Chisinau"
    ]

    static newSampleCreateAccountCommand(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(ACCOUNT_CREATE_COMMAND + map) as String, CreateAccountCommand.class)
    }

    static newSampleAccountEvent(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(ACCOUNT_CREATED_EVENT + map) as String, AccountCreated.class)
    }
}
