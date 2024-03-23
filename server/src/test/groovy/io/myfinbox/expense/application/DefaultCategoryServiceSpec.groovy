package io.myfinbox.expense.application

import io.myfinbox.expense.domain.AccountIdentifier
import io.myfinbox.expense.domain.Categories
import io.myfinbox.expense.domain.Category
import io.myfinbox.expense.domain.DefaultCategories
import io.myfinbox.shared.Failure
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.expense.DataSamples.*
import static java.lang.Boolean.FALSE
import static java.lang.Boolean.TRUE
import static org.apache.commons.lang3.RandomStringUtils.random
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.asCollection
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.intersect

@Tag("unit")
class DefaultCategoryServiceSpec extends Specification {

    Categories categories
    CategoryService service

    def setup() {
        categories = Mock()
        service = new DefaultCategoryService(categories)
    }

    def "should fail create default categories when account is null"() {
        when: 'account is null'
        def either = service.createDefault(null)

        then: 'validation failure is present'
        assert either.isLeft()

        and: 'failure message is account cannot be null'
        assert either.getLeft() == Failure.ofValidation("AccountIdentifier cannot be null", List.of())
    }

    def "should create default categories"() {
        def comp = { a, b -> a.name <=> b.name ?: a.account.id() <=> b.account.id() } as Comparator<? super Category>

        setup: 'repository mock behavior and interaction'
        def account = new AccountIdentifier(UUID.randomUUID())
        1 * categories.saveAll { actual ->
            intersect(asCollection(actual), newSampleDefaultCategories(account), comp).size() == DefaultCategories.values().size()
        } >> []

        expect: 'created default categories for provided accountId'
        service.createDefault(account)
    }

    def "should fail category creation when accountId is null"() {
        given: 'new command with null accountId'
        def command = newSampleExpenseCategoryCommand(accountId: null)

        when: 'expense category fails to create'
        def either = service.create(command)

        then: 'failure result is present'
        assert either.isLeft()

        and: 'failure result contains accountId failure message'
        assert either.getLeft() == Failure.ofValidation(DefaultCategoryService.VALIDATION_CREATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('accountId')
                        .message('AccountId cannot be null.')
                        .build()
        ])
    }

    def "should fail category creation when name is not valid"() {
        given: 'new command with invalid name'
        def command = newSampleExpenseCategoryCommand(name: categoryName)

        when: 'expense category fails to create'
        def either = service.create(command)

        then: 'failure result is present'
        assert either.isLeft()

        and: 'validation failure on name field'
        assert either.getLeft() == Failure.ofValidation(DefaultCategoryService.VALIDATION_CREATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('name')
                        .message(failMessage)
                        .rejectedValue(categoryName)
                        .build()
        ])

        where:
        categoryName | failMessage
        null         | 'Name cannot be empty.'
        ''           | 'Name cannot be empty.'
        '   '        | 'Name cannot be empty.'
        randStr(256) | "Name length cannot be more than ${Category.NAME_MAX_LENGTH}."
    }

    def "should fail category creation when categoryName duplicate"() {
        setup: 'repository mock behavior and interaction'
        1 * categories.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> TRUE

        when: 'expense category fails to create'
        def either = service.create(newSampleExpenseCategoryCommand())

        then: 'duplicate failure result is present'
        assert either.isLeft()

        and: 'failure result contains category name exists message'
        assert either.getLeft() == Failure.ofConflict(DefaultCategoryService.CATEGORY_NAME_DUPLICATE_MESSAGE)
    }

    def "should create a new expense category"() {
        setup: 'repository mock behavior and interaction'
        1 * categories.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> FALSE

        when: 'new expense category is created'
        def either = service.create(newSampleExpenseCategoryCommand())

        then: 'expense category is persisted'
        assert either.isRight()

        and: 'created value'
        assert either.get() == newSampleCategory(
                id: [id: either.get().getId().id()],
                creationTimestamp: either.get().getCreationTimestamp().toString()
        )

        and: 'repository interaction'
        1 * categories.save(_ as Category)
    }

    static randStr(int len) {
        random(len, true, true)
    }
}
