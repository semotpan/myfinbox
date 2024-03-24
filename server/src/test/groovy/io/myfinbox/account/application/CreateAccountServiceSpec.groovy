package io.myfinbox.account.application


import io.myfinbox.account.domain.Account
import io.myfinbox.account.domain.Accounts
import io.myfinbox.shared.Failure
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.account.DataSamples.newSampleAccount
import static io.myfinbox.account.DataSamples.newSampleCreateAccountCommand
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

    def "should fail account creation when firstName length overflow"() {
        given: 'new command with invalid first name'
        def value = randString(256)
        def command = newSampleCreateAccountCommand(firstName: value)

        when: 'account fails to create'
        def either = service.create(command)

        then: 'validation failure result is present'
        assert either.isLeft()

        and: 'validation failure on firstName field'
        assert either.getLeft() == Failure.ofValidation(CreateAccountService.ERROR_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('firstName')
                        .message("First name length cannot exceed '${Account.MAX_LENGTH}' characters.")
                        .rejectedValue(value)
                        .build()
        ])
    }

    def "should fail account creation when lastName length overflow"() {
        given: 'new command with invalid last name'
        def value = randString(256)
        def command = newSampleCreateAccountCommand(lastName: value)

        when: 'account fails to create'
        def either = service.create(command)

        then: 'validation failure result is present'
        assert either.isLeft()

        and: 'validation failure on lastName field'
        assert either.getLeft() == Failure.ofValidation(CreateAccountService.ERROR_MESSAGE, [
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
        assert either.getLeft() == Failure.ofValidation(CreateAccountService.ERROR_MESSAGE, [
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
        "%s@gmail.com".formatted(randString(256))    | "Email address length cannot exceed '${Account.MAX_LENGTH}' characters."
    }

    def "should create an account"() {
        setup: "accounts persist behavior"
        1 * accounts.save(_ as Account) >> _

        when: 'account is created'
        def either = service.create(newSampleCreateAccountCommand())

        then: 'persistent account is present'
        assert either.isRight()

        and: 'ensure account is build as expected'
        assert either.get() == newSampleAccount(
                id: [id: either.get().getId().id()],
                creationDate: either.get().getCreationDate().toString()
        )
    }

    static randString(int len) {
        random(len, true, true)
    }
}
