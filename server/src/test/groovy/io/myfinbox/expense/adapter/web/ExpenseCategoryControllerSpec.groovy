package io.myfinbox.expense.adapter.web

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.myfinbox.TestServerApplication
import io.myfinbox.expense.DataSamples
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.jdbc.JdbcTestUtils
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.expense.DataSamples.newValidExpenseCategoryResource
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.HttpMethod.PUT
import static org.springframework.http.HttpStatus.*
import static org.springframework.http.MediaType.APPLICATION_JSON

@Tag("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestServerApplication)
@TestPropertySource(locations = "/application-test.properties")
class ExpenseCategoryControllerSpec extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    TestRestTemplate restTemplate

    def cleanup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, 'expensecategory')
    }

    def "Should create a new category expense"() {
        given: 'user wants to create a new category expense'
        def request = newValidExpenseCategoryResource()

        when: 'expense category is created'
        def response = postExpenseCategory(request)

        then: 'response status is created'
        assert response.getStatusCode() == CREATED

        and: 'location header contains the created expense category URL location'
        assert response.getHeaders().getLocation() != null

        and: 'body contains created resource'
        JSONAssert.assertEquals(expectedCreatedResource(response), response.getBody(), LENIENT)
    }

    def "should fail creation when request has validation failures"() {
        given: 'user wants to create a new expense category'
        var request = '{}'

        when: 'expense fails to create'
        var response = postExpenseCategory(request)

        then: 'response has status code unprocessable entity'
        assert response.getStatusCode() == UNPROCESSABLE_ENTITY

        and: 'response body contains validation failure response'
        JSONAssert.assertEquals(expectedCreationFailure(), response.getBody(), LENIENT)
    }

    @Sql('/expense/web/expensecategory-create.sql')
    def "should update an existing category expense name"() {
        given: 'user wants to update an existing category name'
        def request = newValidExpenseCategoryResource(name: 'NEW name')

        when: 'expense category name is updated'
        def response = putExpenseCategory(request)

        then: 'response status is ok'
        assert response.getStatusCode() == OK

        and: 'response body contains updated resource'
        JSONAssert.assertEquals(newValidExpenseCategoryResource([name: "NEW name"]), response.getBody(), LENIENT)
    }

    def "Should fail update when category not found"() {
        given: 'user wants to update an existing category'
        var request = newValidExpenseCategoryResource()

        when: 'expense fails to create'
        var response = putExpenseCategory(request)

        then: 'status code is not found'
        assert response.getStatusCode() == NOT_FOUND

        and: 'response body contains not found failure response'
        JSONAssert.assertEquals(expectUpdateNotFoundFailure(), response.getBody(), LENIENT)
    }

    private postExpenseCategory(String request) {
        restTemplate.postForEntity('/expenses/category', entityRequest(request), String.class)
    }

    private putExpenseCategory(String request) {
        restTemplate.exchange(
                "/expenses/category/${DataSamples.categoryId}",
                PUT,
                entityRequest(request),
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
        newValidExpenseCategoryResource(
                categoryId: idFromLocation(response.getHeaders().getLocation())
        )
    }

    def expectedCreationFailure() {
        def filePath = 'expense/web/expense-category-creation-failure-response.json'
        def failureAsMap = new JsonSlurper().parse(new ClassPathResource(filePath).getFile())
        JsonOutput.toJson(failureAsMap)
    }

    def expectUpdateNotFoundFailure() {
        JsonOutput.toJson([
                status   : 404,
                errorCode: 'NOT_FOUND',
                message  : 'Category not found.'
        ])
    }
}
