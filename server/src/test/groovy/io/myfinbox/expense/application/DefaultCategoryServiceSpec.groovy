package io.myfinbox.expense.application

import io.myfinbox.expense.domain.*
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
    Expenses expenses
    CategoryService service

    def setup() {
        categories = Mock()
        expenses = Mock()
        service = new DefaultCategoryService(categories, expenses)
    }

    def "should fail to create default categories when account is null"() {
        when: 'account is null'
        def either = service.createDefault(null)

        then: 'validation failure should occur'
        assert either.isLeft()

        and: 'failure message should indicate account cannot be null'
        assert either.getLeft() == Failure.ofValidation("AccountIdentifier cannot be null", List.of())
    }

    def "should successfully create default categories for the provided account"() {
        given: 'mock repository behavior and interaction'
        def account = new AccountIdentifier(UUID.randomUUID())
        def expectedCategories = newSampleDefaultCategories(account)
        // Define comparator to compare categories by name and account ID
        def comp = { a, b -> a.name <=> b.name ?: a.account.id() <=> b.account.id() } as Comparator<? super Category>

        // Mock saving default categories and verify if the correct categories are saved
        1 * categories.saveAll { actual ->
            intersect(asCollection(actual), expectedCategories, comp).size() == DefaultCategories.values().size()
        } >> []

        when: 'creating default categories'
        def result = service.createDefault(account)

        then: 'default categories are created successfully'
        assert result.isRight()
    }

    def "should fail category creation when accountId is null"() {
        given: 'a new command with a null accountId'
        def command = newSampleExpenseCategoryCommand(accountId: null)

        when: 'attempting to create an expense category'
        def either = service.create(command)

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should contain an error message about the null accountId'
        assert either.getLeft() == Failure.ofValidation(DefaultCategoryService.VALIDATION_CREATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('accountId')
                        .message('AccountId cannot be null.')
                        .build()
        ])
    }

    def "should fail category creation when name is not valid"() {
        given: 'a new command with an invalid name'
        def command = newSampleExpenseCategoryCommand(name: categoryName)

        when: 'attempting to create an expense category'
        def either = service.create(command)

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'validation failure should occur on the name field'
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
        randStr(256) | "Name length cannot exceed ${Category.NAME_MAX_LENGTH} characters."
    }

    def "should fail category creation when name is duplicate"() {
        setup: 'mock repository behavior to indicate name duplication'
        1 * categories.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> TRUE

        when: 'attempting to create an expense category'
        def either = service.create(newSampleExpenseCategoryCommand())

        then: 'a failure result indicating name duplication should occur'
        assert either.isLeft()

        and: 'the failure result should contain a message about the duplicate category name'
        assert either.getLeft() == Failure.ofConflict(DefaultCategoryService.CATEGORY_NAME_DUPLICATE_MESSAGE)
    }

    def "should successfully create a new expense category"() {
        setup: 'mock repository behavior to indicate no name duplication'
        1 * categories.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> FALSE

        when: 'creating a new expense category'
        def either = service.create(newSampleExpenseCategoryCommand())

        then: 'the creation operation should succeed'
        assert either.isRight()

        and: 'the created category should have correct attributes'
        assert either.get() == newSampleCategory(
                id: [id: either.get().getId().id()],
                creationTimestamp: either.get().getCreationTimestamp().toString()
        )

        and: 'the category should be persisted in the repository'
        1 * categories.save(_ as Category)
    }

    def "should fail category update when accountId is null"() {
        given: 'a new command with a null accountId'
        def command = newSampleExpenseCategoryCommand(accountId: null)

        when: 'attempting to update an expense category'
        def either = service.update(UUID.randomUUID(), command)

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should contain an error message about the null accountId'
        assert either.getLeft() == Failure.ofValidation(DefaultCategoryService.VALIDATION_UPDATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('accountId')
                        .message('AccountId cannot be null.')
                        .build()
        ])
    }

    def "should fail category update when name is not valid"() {
        given: 'a new command with an invalid name'
        def command = newSampleExpenseCategoryCommand(name: categoryName)

        when: 'attempting to update an expense category'
        def either = service.update(UUID.randomUUID(), command)

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'validation failure should occur on the name field'
        assert either.getLeft() == Failure.ofValidation(DefaultCategoryService.VALIDATION_UPDATE_FAILURE_MESSAGE, [
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
        randStr(256) | "Name length cannot exceed ${Category.NAME_MAX_LENGTH} characters."
    }

    def "should fail category update when category not found, null categoryId"() {
        given: 'a valid command'
        def command = newSampleExpenseCategoryCommand()

        when: 'attempting to update an expense category with null categoryId'
        def either = service.update(null, command)

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should indicate category not found'
        assert either.getLeft() == Failure.ofNotFound(DefaultCategoryService.CATEGORY_NOT_FOUND_MESSAGE)
    }

    def "should fail category update when category not found, no DB existence"() {
        setup: 'a non-existing category in the database'
        1 * categories.findByIdAndAccount(_ as Category.CategoryIdentifier, _ as AccountIdentifier) >> Optional.empty()

        when: 'attempting to update an expense category'
        def either = service.update(UUID.randomUUID(), newSampleExpenseCategoryCommand())

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should indicate category not found'
        assert either.getLeft() == Failure.ofNotFound(DefaultCategoryService.CATEGORY_NOT_FOUND_MESSAGE)
    }

    def "should not update category name when name not changed"() {
        setup: 'a non-existing category in the database'
        1 * categories.findByIdAndAccount(_ as Category.CategoryIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleCategory())

        when: 'attempting to update an expense category without changing the name'
        def either = service.update(UUID.randomUUID(), newSampleExpenseCategoryCommand())

        then: 'no update should occur'
        assert either.isRight()

        and: 'the same category should be returned'
        assert either.get() == newSampleCategory()

        and: 'no database save operation should be performed'
        0 * categories.save(_ as Category)
    }

    def "should fail category update when name is duplicate"() {
        setup: 'an existing category and an existing category name'
        1 * categories.findByIdAndAccount(_ as Category.CategoryIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleCategory())
        1 * categories.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> TRUE

        when: 'attempting to update an expense category with a duplicate name'
        def either = service.update(UUID.randomUUID(), newSampleExpenseCategoryCommand(name: 'Learning'))

        then: 'a failure result indicating name duplication should occur'
        assert either.isLeft()

        and: 'the failure result should contain a message about the duplicate category name'
        assert either.getLeft() == Failure.ofConflict(DefaultCategoryService.CATEGORY_NAME_DUPLICATE_MESSAGE)
    }

    def "should update an existing expense category with a new name"() {
        setup: 'an existing category and a non-existing category name'
        1 * categories.findByIdAndAccount(_ as Category.CategoryIdentifier, _ as AccountIdentifier) >> Optional.of(newSampleCategory())
        1 * categories.existsByNameAndAccount(_ as String, _ as AccountIdentifier) >> FALSE

        when: 'attempting to update an expense category with a new name'
        def either = service.update(UUID.randomUUID(), newSampleExpenseCategoryCommand(name: 'Learning'))

        then: 'the expense category should be updated successfully'
        assert either.isRight()

        and: 'the updated category should have the new name'
        assert either.get() == newSampleCategory(name: 'Learning')

        and: 'the updated category should be saved in the repository'
        1 * categories.save(_ as Category)
    }

    def "should fail to delete category when category not found, categoryId is null"() {
        given: 'a null categoryId'
        def categoryId = null

        when: 'attempting to delete a category with null categoryId'
        def either = service.delete(categoryId)

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should indicate category not found'
        assert either.getLeft() == Failure.ofNotFound(DefaultCategoryService.CATEGORY_NOT_FOUND_MESSAGE)
    }

    def "should fail to delete category when category not found in the database"() {
        setup: 'repository with no existing category'
        1 * categories.findById(_ as Category.CategoryIdentifier) >> Optional.empty()

        when: 'attempting to delete a category that does not exist in the database'
        def either = service.delete(UUID.randomUUID())

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should indicate category not found'
        assert either.getLeft() == Failure.ofNotFound(DefaultCategoryService.CATEGORY_NOT_FOUND_MESSAGE)
    }

    def "should fail to delete category when category is in use"() {
        setup: 'repository with an existing category and existing expenses associated with it'
        1 * categories.findById(_ as Category.CategoryIdentifier) >> Optional.of(newSampleCategory())
        1 * expenses.existsByCategory(_ as Category) >> TRUE

        when: 'attempting to delete a category that is currently in use'
        def either = service.delete(UUID.randomUUID())

        then: 'a failure result is expected'
        assert either.isLeft()

        and: 'the failure result should indicate category in use'
        assert either.getLeft() == Failure.ofConflict(DefaultCategoryService.CATEGORY_IN_USE_FAILURE_MESSAGE)
    }

    def "should successfully delete a category when no expenses are associated with it"() {
        setup: 'repository with an existing category and no associated expenses'
        1 * categories.findById(_ as Category.CategoryIdentifier) >> Optional.of(newSampleCategory())
        1 * expenses.existsByCategory(_ as Category) >> FALSE

        when: 'attempting to delete a category with no associated expenses'
        def either = service.delete(UUID.randomUUID())

        then: 'no result is present'
        assert either.isRight()

        and: 'the category should be deleted from the repository'
        1 * categories.delete(_ as Category)
    }

    static randStr(int len) {
        random(len, true, true)
    }
}
