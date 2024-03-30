package io.myfinbox.income.application

import io.myfinbox.income.domain.*
import io.myfinbox.shared.Failure
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.income.DataSamples.*
import static java.lang.Boolean.FALSE
import static java.lang.Boolean.TRUE
import static org.apache.commons.lang3.RandomStringUtils.random
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asCollection
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.intersect

@Tag("unit")
class DefaultIncomeSourceServiceSpec extends Specification {

    IncomeSources incomeSources
    Incomes incomes
    IncomeSourceService service

    def setup() {
        incomeSources = Mock()
        incomes = Mock()
        service = new DefaultIncomeSourceService(incomeSources, incomes)
    }

    def "should fail create default income sources when account is null"() {
        when: 'account is null'
        def either = service.createDefault(null)

        then: 'validation failure is present'
        assert either.isLeft()

        and: 'failure message is account cannot be null'
        assert either.getLeft() == Failure.ofValidation("AccountIdentifier cannot be null", [])
    }

    def "should create income sources"() {
        def comp = { a, b -> a.name <=> b.name ?: a.account.id() <=> b.account.id() } as Comparator<? super IncomeSource>

        setup: 'repository mock behavior and interaction'
        def account = new AccountIdentifier(UUID.randomUUID())
        1 * incomeSources.saveAll { actual ->
            intersect(asCollection(actual), newSampleDefaultSources(account), comp).size() == DefaultIncomeSources.values().size()
        } >> []

        expect: 'created default income sources for provided accountId'
        service.createDefault(account)
    }

    def "should fail income source creation when accountId is null"() {
        given: 'a new command with a null accountId'
        def command = newSampleIncomeSourceCommand(accountId: null)

        when: 'attempting to create an income source'
        def either = service.create(command)

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should contain an error message about the null accountId'
        assert either.getLeft() == Failure.ofValidation(DefaultIncomeSourceService.VALIDATION_CREATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('accountId')
                        .message('AccountId cannot be null.')
                        .build()
        ])
    }

    def "should fail income source creation when name is not valid"() {
        given: 'a new command with an invalid name'
        def command = newSampleIncomeSourceCommand(name: incomeSourceName)

        when: 'attempting to create an income source'
        def either = service.create(command)

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'validation failure should occur on the name field'
        assert either.getLeft() == Failure.ofValidation(DefaultIncomeSourceService.VALIDATION_CREATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('name')
                        .message(failMessage)
                        .rejectedValue(incomeSourceName)
                        .build()
        ])

        where:
        incomeSourceName | failMessage
        null             | 'Name cannot be empty.'
        ''               | 'Name cannot be empty.'
        '   '            | 'Name cannot be empty.'
        randStr(256)     | "Name length cannot exceed ${IncomeSource.NAME_MAX_LENGTH} characters."
    }

    def "should fail income source creation when name is duplicate"() {
        setup: 'mock repository behavior to indicate name duplication'
        1 * incomeSources.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> TRUE

        when: 'attempting to create an expense category'
        def either = service.create(newSampleIncomeSourceCommand())

        then: 'a failure result indicating name duplication should occur'
        assert either.isLeft()

        and: 'the failure result should contain a message about the duplicate income source name'
        assert either.getLeft() == Failure.ofConflict(DefaultIncomeSourceService.SOURCE_NAME_DUPLICATE_MESSAGE)
    }

    def "should successfully create a new income source"() {
        setup: 'mock repository behavior to indicate no name duplication'
        1 * incomeSources.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> FALSE

        when: 'creating a new income source'
        def either = service.create(newSampleIncomeSourceCommand())

        then: 'the creation operation should succeed'
        assert either.isRight()

        and: 'the created income source should have correct attributes'
        assert either.get() == newSampleIncomeSource(
                id: [id: either.get().getId().id()],
                creationTimestamp: either.get().getCreationTimestamp().toString()
        )

        and: 'the income source should be persisted in the repository'
        1 * incomeSources.save(_ as IncomeSource)
    }

    def "should fail income source update when accountId is null"() {
        given: 'a new command with a null accountId'
        def command = newSampleIncomeSourceCommand(accountId: null)

        when: 'attempting to update an income source'
        def either = service.update(UUID.randomUUID(), command)

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should contain an error message about the null accountId'
        assert either.getLeft() == Failure.ofValidation(DefaultIncomeSourceService.VALIDATION_UPDATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('accountId')
                        .message('AccountId cannot be null.')
                        .build()
        ])
    }

    def "should fail income source update when name is not valid"() {
        given: 'a new command with an invalid name'
        def command = newSampleIncomeSourceCommand(name: incomeSourceName)

        when: 'attempting to update an income source'
        def either = service.update(UUID.randomUUID(), command)

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'validation failure should occur on the name field'
        assert either.getLeft() == Failure.ofValidation(DefaultIncomeSourceService.VALIDATION_UPDATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('name')
                        .message(failMessage)
                        .rejectedValue(incomeSourceName)
                        .build()
        ])

        where:
        incomeSourceName | failMessage
        null             | 'Name cannot be empty.'
        ''               | 'Name cannot be empty.'
        '   '            | 'Name cannot be empty.'
        randStr(256)     | "Name length cannot exceed ${IncomeSource.NAME_MAX_LENGTH} characters."
    }

    def "should fail income source update when income source not found, null income source"() {
        given: 'a valid command'
        def command = newSampleIncomeSourceCommand()

        when: 'attempting to update an income source with null incomeSourceId'
        def either = service.update(null, command)

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should indicate income source not found'
        assert either.getLeft() == Failure.ofNotFound(DefaultIncomeSourceService.SOURCE_NOT_FOUND_MESSAGE)
    }

    def "should fail income source update when income source not found, no DB existence"() {
        setup: 'a non-existing income source in the database'
        1 * incomeSources.findByIdAndAccount(_ as IncomeSource.IncomeSourceIdentifier, _ as AccountIdentifier) >> Optional.empty()

        when: 'attempting to update an income source'
        def either = service.update(UUID.randomUUID(), newSampleIncomeSourceCommand())

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should indicate income source not found'
        assert either.getLeft() == Failure.ofNotFound(DefaultIncomeSourceService.SOURCE_NOT_FOUND_MESSAGE)
    }

    def "should not update income source name when name not changed"() {
        setup: 'a non-existing income source in the database'
        1 * incomeSources.findByIdAndAccount(_ as IncomeSource.IncomeSourceIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleIncomeSource())

        when: 'attempting to update an income source without changing the name'
        def either = service.update(UUID.randomUUID(), newSampleIncomeSourceCommand())

        then: 'no update should occur'
        assert either.isRight()

        and: 'the same income source should be returned'
        assert either.get() == newSampleIncomeSource()

        and: 'no database save operation should be performed'
        0 * incomeSources.save(_ as IncomeSource)
    }

    def "should fail income source update when name is duplicate"() {
        setup: 'an existing income source and an existing income source name'
        1 * incomeSources.findByIdAndAccount(_ as IncomeSource.IncomeSourceIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleIncomeSource())
        1 * incomeSources.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> TRUE

        when: 'attempting to update an income source with a duplicate name'
        def either = service.update(UUID.randomUUID(), newSampleIncomeSourceCommand(name: 'Learning'))

        then: 'a failure result indicating name duplication should occur'
        assert either.isLeft()

        and: 'the failure result should contain a message about the duplicate income source name'
        assert either.getLeft() == Failure.ofConflict(DefaultIncomeSourceService.SOURCE_NAME_DUPLICATE_MESSAGE)
    }

    def "should update an existing income source with a new name"() {
        setup: 'an existing income source and a non-existing income source name'
        1 * incomeSources.findByIdAndAccount(_ as IncomeSource.IncomeSourceIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleIncomeSource())
        1 * incomeSources.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> FALSE

        when: 'attempting to update an income source with a new name'
        def either = service.update(UUID.randomUUID(), newSampleIncomeSourceCommand(name: 'Learning'))

        then: 'the income source should be updated successfully'
        assert either.isRight()

        and: 'the updated income source should have the new name'
        assert either.get() == newSampleIncomeSource(name: 'Learning')

        and: 'the updated income source should be saved in the repository'
        1 * incomeSources.save(_ as IncomeSource)
    }

    def "should fail to delete income source when income source not found, incomeSourceId is null"() {
        given: 'a null incomeSourceId'
        def categoryId = null

        when: 'attempting to delete a income source with null incomeSourceId'
        def either = service.delete(categoryId)

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should indicate income source not found'
        assert either.getLeft() == Failure.ofNotFound(DefaultIncomeSourceService.SOURCE_NOT_FOUND_MESSAGE)
    }

    def "should fail to delete income source when income source not found in the database"() {
        setup: 'repository with no existing income source'
        1 * incomeSources.findById(_ as IncomeSource.IncomeSourceIdentifier) >> Optional.empty()

        when: 'attempting to delete a income source that does not exist in the database'
        def either = service.delete(UUID.randomUUID())

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should indicate income source not found'
        assert either.getLeft() == Failure.ofNotFound(DefaultIncomeSourceService.SOURCE_NOT_FOUND_MESSAGE)
    }

    def "should fail to delete income source when income source is in use"() {
        setup: 'repository with an existing income source and existing incomes associated with it'
        1 * incomeSources.findById(_ as IncomeSource.IncomeSourceIdentifier) >> Optional.of(newSampleIncomeSource())
        1 * incomes.existsByIncomeSource(_ as IncomeSource) >> TRUE

        when: 'attempting to delete a income source that is currently in use'
        def either = service.delete(UUID.randomUUID())

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should indicate income source in use'
        assert either.getLeft() == Failure.ofConflict(DefaultIncomeSourceService.SOURCE_IN_USE_FAILURE_MESSAGE)
    }

    def "should successfully delete a income source when no incomes are associated with it"() {
        setup: 'repository with an existing income source and no associated incomes'
        1 * incomeSources.findById(_ as IncomeSource.IncomeSourceIdentifier) >> Optional.of(newSampleIncomeSource())
        1 * incomes.existsByIncomeSource(_ as IncomeSource) >> FALSE

        when: 'attempting to delete a income source with no associated incomes'
        def either = service.delete(UUID.randomUUID())

        then: 'no result is present'
        assert either.isRight()

        and: 'the income source should be deleted from the repository'
        1 * incomeSources.delete(_ as IncomeSource)
    }

    static randStr(int len) {
        random(len, true, true)
    }
}
