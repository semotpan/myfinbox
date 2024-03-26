package io.myfinbox.expense.adapter.web

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.myfinbox.TestServerApplication
import io.myfinbox.expense.ExpenseCreated
import io.myfinbox.expense.ExpenseDeleted
import io.myfinbox.expense.ExpenseUpdated
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.modulith.test.ApplicationModuleTest
import org.springframework.modulith.test.PublishedEvents
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.jdbc.JdbcTestUtils
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.expense.DataSamples.*
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.HttpMethod.DELETE
import static org.springframework.http.HttpMethod.PUT
import static org.springframework.http.HttpStatus.*
import static org.springframework.http.MediaType.APPLICATION_JSON

@Tag("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestServerApplication)
@ApplicationModuleTest
@TestPropertySource(locations = "/application-test.properties")
class ExpenseControllerSpec extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    PublishedEvents events

    @Autowired
    TestRestTemplate restTemplate

    def cleanup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, 'expenses', 'expensecategory')
    }

    @Sql('/expense/web/expensecategory-create.sql')
    def "should create a new expense"() {
        given: 'user wants to create a new expense'
        var request = newValidExpenseResource()

        when: 'expense is created'
        var response = postNewExpense(request)

        then: 'response status is created'
        assert response.getStatusCode() == CREATED

        and: 'location header contains the created expense URL location'
        assert response.getHeaders().getLocation() != null

        and: 'body contains created resource'
        JSONAssert.assertEquals(expectedCreatedResource(response), response.getBody(), LENIENT)

        and: 'expense created event raised'
        assert events.ofType(ExpenseCreated.class).contains(
                newSampleExpenseCreatedEvent(expenseId: idFromLocation(response.getHeaders().getLocation()))
        )
    }

    def "should fail creation when request has validation failures"() {
        given: 'user wants to create a new expense'
        var request = '{}'

        when: 'expense fails to create'
        var response = postNewExpense(request)

        then: 'response has status code unprocessable entity'
        assert response.getStatusCode() == UNPROCESSABLE_ENTITY

        and: 'response body contains validation failure response'
        JSONAssert.assertEquals(expectedCreationFailure(), response.getBody(), LENIENT)
    }

    @Sql(['/expense/web/expensecategory-create.sql', '/expense/web/expense-create.sql'])
    def "should update an expense"() {
        given: 'user wants to update an existing expense'
        var request = newValidExpenseResource(
                categoryId: categoryId2,
                paymentType: "Card",
                amount: 50,
                currencyCode: "MDL",
                expenseDate: '2024-03-19',
                description: 'My hobbies'
        )

        when: 'expense is updated'
        var response = putAnExpense(request)

        then: 'response status is ok'
        assert response.getStatusCode() == OK

        and: 'body contains updated resource'
        JSONAssert.assertEquals(expectedUpdatedResource(), response.getBody(), LENIENT)

        and: 'expense updated event raised'
        assert events.ofType(ExpenseUpdated.class).contains(
                newSampleExpenseUpdatedCreatedEvent(
                        categoryId: categoryId2,
                        paymentType: "CARD",
                        amount: [
                                amount  : 50,
                                currency: "MDL"
                        ],
                        expenseDate: '2024-03-19'
                )
        )
    }

    @Sql(['/expense/web/expensecategory-create.sql', '/expense/web/expense-create.sql'])
    def "should fail updating when provided category not found"() {
        given: 'user wants to update an existing expense'
        var request = newValidExpenseResource(
                categoryId: UUID.randomUUID()
        )

        when: 'expense fails to update'
        var response = postNewExpense(request)

        then: 'response has status code not found'
        assert response.getStatusCode() == NOT_FOUND

        and: 'response body contains not found failure response'
        JSONAssert.assertEquals(expectedUpdateFailure(), response.getBody(), LENIENT)
    }

    @Sql(['/expense/web/expensecategory-create.sql', '/expense/web/expense-create.sql'])
    def "should delete an expense"() {
        when: 'expense is deleted'
        var response = deleteAnExpense()

        then: 'response status is created'
        assert response.getStatusCode() == NO_CONTENT

        and: 'expense deleted event raised'
        assert events.ofType(ExpenseDeleted.class).contains(newSampleExpenseDeletedEvent())
    }

    def "should fail delete when expense not found"() {
        when: 'expense fails to delete'
        var response = deleteAnExpense()

        then: 'response has status code not found'
        assert response.getStatusCode() == NOT_FOUND

        and: 'response body contains not found failure response'
        JSONAssert.assertEquals(expectedDeleteFailure(), response.getBody(), LENIENT)
    }

    def postNewExpense(String req) {
        restTemplate.postForEntity('/expenses', entityRequest(req), String.class)
    }

    def putAnExpense(String req) {
        restTemplate.exchange(
                "/expenses/${expenseId}",
                PUT,
                entityRequest(req),
                String.class
        )
    }

    def deleteAnExpense() {
        restTemplate.exchange(
                "/expenses/${expenseId}",
                DELETE,
                entityRequest(null),
                String.class
        )
    }

    def entityRequest(String req) {
        var headers = new HttpHeaders()
        headers.setContentType(APPLICATION_JSON)

        new HttpEntity<>(req, headers)
    }

    UUID idFromLocation(URI location) {
        def paths = location.getPath().split("/")
        return UUID.fromString(paths[paths.length - 1])
    }

    def expectedCreatedResource(ResponseEntity response) {
        newValidExpenseResource(
                expenseId: idFromLocation(response.getHeaders().getLocation())
        )
    }

    def expectedUpdatedResource() {
        newValidExpenseResource(
                categoryId: categoryId2,
                paymentType: "Card",
                amount: 50,
                currencyCode: "MDL",
                expenseDate: '2024-03-19',
                description: 'My hobbies'
        )
    }

    def expectedCreationFailure() {
        def filePath = 'expense/web/expense-creation-failure-response.json'
        def failureAsMap = new JsonSlurper().parse(new ClassPathResource(filePath).getFile())
        JsonOutput.toJson(failureAsMap)
    }

    def expectedUpdateFailure() {
        JsonOutput.toJson([
                status   : 404,
                errorCode: "NOT_FOUND",
                message  : "Category not found for the provided account."
        ])
    }

    def expectedDeleteFailure() {
        JsonOutput.toJson([
                status   : 404,
                errorCode: "NOT_FOUND",
                message  : "Expense was not found."
        ])
    }
}
