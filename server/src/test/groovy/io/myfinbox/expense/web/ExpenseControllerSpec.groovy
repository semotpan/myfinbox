package io.myfinbox.expense.web

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.myfinbox.TestServerApplication
import io.myfinbox.expense.ExpenseCreated
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

import static io.myfinbox.expense.DataSamples.newSampleExpenseCreatedEvent
import static io.myfinbox.expense.DataSamples.newValidExpenseResource
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
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

    def postNewExpense(String req) {
        restTemplate.postForEntity('/expenses', entityRequest(req), String.class)
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

    def expectedCreationFailure() {
        def filePath = 'expense/web/expense-creation-failure-response.json'
        def failureAsMap = new JsonSlurper().parse(new ClassPathResource(filePath).getFile())
        JsonOutput.toJson(failureAsMap)
    }
}
