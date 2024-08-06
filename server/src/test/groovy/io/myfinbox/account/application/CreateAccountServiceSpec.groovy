package io.myfinbox.account.application

import io.myfinbox.account.domain.*
import io.myfinbox.shared.Failure
import spock.lang.Specification
import spock.lang.Tag

import java.time.Instant
import java.time.ZoneId

import static io.myfinbox.account.DataSamples.newSampleCreateAccountCommand
import static io.myfinbox.account.application.CreateAccountService.ERROR_MESSAGE
import static org.apache.commons.lang3.RandomStringUtils.random

@Tag("unit")
class CreateAccountServiceSpec extends Specification {

    static EMAIL_EMPTY_FIELD_ERROR = "Email address cannot be empty."
    static RFC_EMAIL_FIELD_ERROR = "Email address must follow RFC 5322 standard."

    Accounts accounts
    CreateAccountService service

    def setup() {
        accounts = Mock()
        service = new CreateAccountService(accounts)
    }

    def "should fail account creation when firstName length exceeds limit"() {
        given: 'new command with invalid first name'
        def value = randomString(256)
        def command = newSampleCreateAccountCommand(firstName: value)

        when: 'account fails to create'
        def either = service.create(command)

        then: 'validation failure result is present'
        assert either.isLeft()

        and: 'validation failure on firstName field'
        assert either.getLeft() == Failure.ofValidation(ERROR_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('firstName')
                        .message("First name length cannot exceed '${Account.MAX_LENGTH}' characters.")
                        .rejectedValue(value)
                        .build()
        ])
    }

    def "should fail account creation when lastName length exceeds limit"() {
        given: 'new command with invalid last name'
        def value = randomString(256)
        def command = newSampleCreateAccountCommand(lastName: value)

        when: 'account fails to create'
        def either = service.create(command)

        then: 'validation failure result is present'
        assert either.isLeft()

        and: 'validation failure on lastName field'
        assert either.getLeft() == Failure.ofValidation(ERROR_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('lastName')
                        .message("Last name length cannot exceed '${Account.MAX_LENGTH}' characters.")
                        .rejectedValue(value)
                        .build()
        ])
    }

    def "should fail account creation when email address is invalid with message: '#errorMessage'"() {
        given: 'new command with invalid emailAddress'
        def command = newSampleCreateAccountCommand(emailAddress: emailAddress)

        when: 'account fails to create'
        def either = service.create(command)

        then: 'validation failure result is present'
        assert either.isLeft()

        and: 'validation failure error on emailAddress field'
        assert either.getLeft() == Failure.ofValidation(ERROR_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field("emailAddress")
                        .message(errorMessage)
                        .rejectedValue(emailAddress)
                        .build()
        ])

        where:
        emailAddress                                 | errorMessage
        null                                         | EMAIL_EMPTY_FIELD_ERROR
        ''                                           | EMAIL_EMPTY_FIELD_ERROR
        '    '                                       | EMAIL_EMPTY_FIELD_ERROR
        'value+@@gmail.com'                          | RFC_EMAIL_FIELD_ERROR
        'value@gmail..com'                           | RFC_EMAIL_FIELD_ERROR
        '@3mail'                                     | RFC_EMAIL_FIELD_ERROR
        '1111@'                                      | RFC_EMAIL_FIELD_ERROR
        'a3.com'                                     | RFC_EMAIL_FIELD_ERROR
        'aa123@b23'                                  | RFC_EMAIL_FIELD_ERROR
        '@qwe123.er'                                 | RFC_EMAIL_FIELD_ERROR
        'jon.doe@gmailcom'                           | RFC_EMAIL_FIELD_ERROR
        'MOzaRT54@'                                  | RFC_EMAIL_FIELD_ERROR
        'Abc.example.com'                            | RFC_EMAIL_FIELD_ERROR
        'A@b@c@example.com'                          | RFC_EMAIL_FIELD_ERROR
        '_underscore_u@domain_com.con'               | RFC_EMAIL_FIELD_ERROR
        "a\"b(c)d,e:f;g<h>i[j\\k]l@example.com"      | RFC_EMAIL_FIELD_ERROR
        "this is\"not\\allowed@example.com"          | RFC_EMAIL_FIELD_ERROR
        "this\\ still\\\"not\\\\allowed@example.com" | RFC_EMAIL_FIELD_ERROR
        "%s@gmail.com".formatted(randomString(256))  | "Email address length cannot exceed '${EmailAddress.MAX_LENGTH}' characters."
    }

    def "should fail account creation when currency is invalid with message: '#errorMessage'"() {
        given: 'new command with invalid currency'
        def command = newSampleCreateAccountCommand(currency: currency)

        when: 'account fails to create'
        def either = service.create(command)

        then: 'validation failure result is present'
        assert either.isLeft()

        and: 'validation failure error on currency field'
        assert either.getLeft() == Failure.ofValidation(ERROR_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field("currency")
                        .message(errorMessage)
                        .rejectedValue(currency)
                        .build()
        ])

        where:
        currency | errorMessage
        null     | "Currency 'null' is invalid."
        ''       | "Currency '' is invalid."
        '    '   | "Currency '    ' is invalid."
        'MD'     | "Currency 'MD' is invalid."
    }

    def "should fail account creation when zoneId is invalid with message: '#errorMessage'"() {
        given: 'new command with invalid zoneId'
        def command = newSampleCreateAccountCommand(zoneId: zoneId)

        when: 'account fails to create'
        def either = service.create(command)

        then: 'validation failure result is present'
        assert either.isLeft()

        and: 'validation failure error on zoneId field'
        assert either.getLeft() == Failure.ofValidation(ERROR_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field("zoneId")
                        .message(errorMessage)
                        .rejectedValue(zoneId)
                        .build()
        ])

        where:
        zoneId             | errorMessage
        null               | "ZoneId 'null' is invalid."
        ''                 | "ZoneId '' is invalid."
        '    '             | "ZoneId '    ' is invalid."
        'Europe/Chissinau' | "ZoneId 'Europe/Chissinau' is invalid."
    }

    def "should create an account successfully"() {
        setup: "accounts persist behavior"
        1 * accounts.save(_ as Account) >> _

        when: 'account is created'
        def either = service.create(newSampleCreateAccountCommand())

        then: 'persistent account is present'
        assert either.isRight()

        and: 'ensure account is build as expected'
        assert either.get().getId().id() != null
        assert either.get().getId().id() instanceof UUID
        assert either.get().getCreationTimestamp() != null
        assert either.get().getCreationTimestamp() instanceof Instant
        assert either.get().getAccountDetails() == new AccountDetails("Jon", "Snow")
        assert either.get().getEmailAddress() == new EmailAddress("jonsnow@gmail.com")
        assert either.get().getPreference() == new Preference(Currency.getInstance("MDL"), ZoneId.of("Europe/Chisinau"))
    }

    static randomString(int len) {
        random(len, true, true)
    }
}
