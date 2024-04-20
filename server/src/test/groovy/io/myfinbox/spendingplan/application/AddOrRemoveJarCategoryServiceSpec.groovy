package io.myfinbox.spendingplan.application

import io.myfinbox.shared.Failure
import io.myfinbox.spendingplan.domain.JarExpenseCategories
import io.myfinbox.spendingplan.domain.JarIdentifier
import io.myfinbox.spendingplan.domain.Jars
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.spendingplan.DataSamples.*
import static io.myfinbox.spendingplan.application.AddOrRemoveJarCategoryService.*
import static io.myfinbox.spendingplan.domain.JarExpenseCategory.CategoryIdentifier
import static io.myfinbox.spendingplan.domain.Plan.PlanIdentifier
import static java.lang.Boolean.FALSE

@Tag("unit")
class AddOrRemoveJarCategoryServiceSpec extends Specification {

    Jars jars
    JarExpenseCategories jarExpenseCategories
    AddOrRemoveJarCategoryService service

    def setup() {
        jars = Mock()
        jarExpenseCategories = Mock()
        service = new AddOrRemoveJarCategoryService(jars, jarExpenseCategories)
    }

    def "should fail modify categories to jar when invalid categories"() {
        given: 'a new command with invalid categories'
        def command = newSampleJarCategoriesCommand(categories: categories)

        when: 'attempting to add or remove an invalid categories command'
        def either = service.addOrRemove(UUID.randomUUID(), UUID.randomUUID(), command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates validation failure for add or remove categories request'
        assert either.getLeft() == Failure.ofValidation(VALIDATION_CREATE_FAILURE_MESSAGE, [
                Failure.FieldViolation.builder()
                        .field('categories')
                        .message(failMessage)
                        .rejectedValue(command.categories())
                        .build()
        ])

        where:
        categories                                                           | failMessage
        null                                                                 | 'At least one category must be provided.'
        []                                                                   | 'At least one category must be provided.'
        [newSampleJarCategoryToAddAsMap(categoryId: null)]                   | 'Null categoryId not allowed.'
        [newSampleJarCategoryToAddAsMap(), newSampleJarCategoryToAddAsMap()] | 'Duplicate category ids provided.'
    }

    def "should fail modify categories to jar when null plan ID"() {
        given: 'a new command'
        def command = newSampleJarCategoriesCommand(categories: [newSampleJarCategoryToAddAsMap()])

        when: 'attempting to add or remove categories for null plan id'
        def either = service.addOrRemove(null, null, command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates plan not found failure'
        assert either.getLeft() == Failure.ofNotFound(PLAN_NOT_FOUND_MESSAGE)
    }

    def "should fail modify categories to jar when null jar ID"() {
        given: 'a new command'
        def command = newSampleJarCategoriesCommand(categories: [newSampleJarCategoryToAddAsMap()])

        when: 'attempting to add or remove categories for null jar id'
        def either = service.addOrRemove(UUID.randomUUID(), null, command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates jar not found failure'
        assert either.getLeft() == Failure.ofNotFound(JAR_NOT_FOUND_MESSAGE)
    }

    def "should fail modify categories to jar when plan jar not found"() {
        given: 'a new command'
        def command = newSampleJarCategoriesCommand(categories: [newSampleJarCategoryToAddAsMap()])

        1 * jars.findByIdAndPlanId(_ as JarIdentifier, _ as PlanIdentifier) >>Optional.empty()

        when: 'attempting to add or remove categories for null jar id'
        def either = service.addOrRemove(UUID.randomUUID(), UUID.randomUUID(), command)

        then: 'a failure result is present'
        assert either.isLeft()

        and: 'the failure message indicates plan jar not found failure'
        assert either.getLeft() == Failure.ofNotFound(PLAN_JAR_NOT_FOUND_MESSAGE)
    }

    def "should modify categories to plan jar successfully"() {
        given: 'a new command'
        def categoryId2 = UUID.randomUUID()
        def command = newSampleJarCategoriesCommand(categories: [
                newSampleJarCategoryToAddAsMap(toAdd: null), newSampleJarCategoryToAddAsMap(categoryId: categoryId2, toAdd: false)
        ])

        1 * jars.findByIdAndPlanId(_ as JarIdentifier, _ as PlanIdentifier) >> Optional.of(newSampleJar())
        1 * jarExpenseCategories.existsByJarIdAndCategoryId(new JarIdentifier(UUID.fromString(jarId)), new CategoryIdentifier(UUID.fromString(jarCategoryId))) >> FALSE

        when: 'attempting to add or remove categories for null jar id'
        def either = service.addOrRemove(UUID.randomUUID(), UUID.fromString(jarId), command)

        then: 'a success result is present'
        assert either.isRight()

        and: 'the added categories are present'
        assert either.get().size() == 1
        assert either.get().getFirst().getCategoryId() == new CategoryIdentifier(UUID.fromString(jarCategoryId))
        assert either.get().getFirst().getJar() == newSampleJar()
        assert either.get().getFirst().getCreationTimestamp() != null

        and: 'jar to remove was invoked'
        1 * jarExpenseCategories.deleteByJarIdAndCategoryId(new JarIdentifier(UUID.fromString(jarId)), new CategoryIdentifier(categoryId2))

        and: 'jar to add was invoked'
        1 * jarExpenseCategories.saveAll(_ as List)
    }
}
