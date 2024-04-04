package io.myfinbox.account

import com.fasterxml.jackson.databind.json.JsonMapper
import groovy.json.JsonOutput
import io.myfinbox.account.domain.Account

import static io.myfinbox.account.application.CreateAccountUseCase.CreateAccountCommand

class DataSamples {

    static MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build()

    static ACCOUNT = [
            id          : ["id": "e2709aa2-7907-4f78-98b6-0f36a0c1b5ca"],
            emailAddress: ["emailAddress": "jonsnow@gmail.com"],
            firstName   : "Jon",
            lastName    : "Snow",
            creationDate: "2024-03-17T15:15:04.224870Z"
    ]

    static CREATE_ACCOUNT_RESOURCE = [
            firstName   : "Jon",
            lastName    : "Snow",
            emailAddress: "jonsnow@gmail.com"
    ]

    static ACCOUNT_CREATED_EVENT = [
            accountId   : "e2709aa2-7907-4f78-98b6-0f36a0c1b5ca",
            emailAddress: "jonsnow@gmail.com",
            firstName   : "Jon",
            lastName    : "Snow"
    ]

    static newSampleCreateAccountCommand(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(CREATE_ACCOUNT_RESOURCE + map) as String, CreateAccountCommand.class)
    }

    static newSampleAccount(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(ACCOUNT + map) as String, Account.class)
    }

    static newSampleAccountEvent(map = [:]) {
        MAPPER.readValue(JsonOutput.toJson(ACCOUNT_CREATED_EVENT + map) as String, AccountCreated.class)
    }
}
