package io.myfinbox.income.adapter.web

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.myfinbox.TestServerApplication
import io.myfinbox.income.DataSamples
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

import static io.myfinbox.income.DataSamples.newValidIncomeSourceResource
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.HttpMethod.DELETE
import static org.springframework.http.HttpMethod.PUT
import static org.springframework.http.HttpStatus.*
import static org.springframework.http.MediaType.APPLICATION_JSON

@Tag("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestServerApplication)
@TestPropertySource(locations = "/application-test.properties")
class IncomeSourceControllerSpec extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    TestRestTemplate restTemplate

    def cleanup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, 'incomes', 'incomesource')
    }

    def "Should create a new income source"() {
        given: 'user wants to create a new income source'
        def request = newValidIncomeSourceResource()

        when: 'income source is created'
        def response = postIncomeSource(request)

        then: 'response status is created'
        assert response.getStatusCode() == CREATED

        and: 'location header contains the created income source URL location'
        assert response.getHeaders().getLocation() != null

        and: 'body contains created resource'
        JSONAssert.assertEquals(expectedCreatedResource(response), response.getBody(), LENIENT)
    }

    def "should fail income source creation when request has validation failures"() {
        given: 'user wants to create a new income source'
        var request = '{}'

        when: 'income source fails to create'
        var response = postIncomeSource(request)

        then: 'response has status code unprocessable entity'
        assert response.getStatusCode() == UNPROCESSABLE_ENTITY

        and: 'response body contains validation failure response'
        JSONAssert.assertEquals(expectedCreationFailure(), response.getBody(), LENIENT)
    }

    @Sql('/income/web/incomesource-create.sql')
    def "should update an existing income source name"() {
        given: 'user wants to update an existing income source name'
        def request = newValidIncomeSourceResource(name: 'NEW name')

        when: 'income source name is updated'
        def response = putIncomeSource(request)

        then: 'response status is ok'
        assert response.getStatusCode() == OK

        and: 'response body contains updated resource'
        JSONAssert.assertEquals(newValidIncomeSourceResource([name: "NEW name"]), response.getBody(), LENIENT)
    }

    def "Should fail update when income source not found"() {
        given: 'user wants to update an existing income source'
        var request = newValidIncomeSourceResource()

        when: 'income source fails to create'
        var response = putIncomeSource(request)

        then: 'status code is not found'
        assert response.getStatusCode() == NOT_FOUND

        and: 'response body contains not found failure response'
        JSONAssert.assertEquals(expectUpdateNotFoundFailure(), response.getBody(), LENIENT)
    }

    @Sql('/income/web/incomesource-create.sql')
    def "should delete an income source"() {
        when: 'income source is deleted'
        def response = deleteIncomeSource()

        then: 'response status is no content'
        assert response.getStatusCode() == NO_CONTENT
    }

    @Sql(['/income/web/incomesource-create.sql', '/income/web/income-create.sql'])
    def "Should fail delete when income source is in use"() {
        when: 'income source fails to delete'
        def response = deleteIncomeSource()

        then: 'response status is conflict'
        assert response.getStatusCode() == CONFLICT

        and: 'response body contains conflict failure response'
        JSONAssert.assertEquals(expectDeleteConflictFailure(), response.getBody(), LENIENT)
    }

    private postIncomeSource(String request) {
        restTemplate.postForEntity('/v1/incomes/income-source', entityRequest(request), String.class)
    }

    private putIncomeSource(String request) {
        restTemplate.exchange(
                "/v1/incomes/income-source/${DataSamples.incomeSourceId}",
                PUT,
                entityRequest(request),
                String.class
        )
    }

    private deleteIncomeSource() {
        restTemplate.exchange(
                "/v1/incomes/income-source/${DataSamples.incomeSourceId}",
                DELETE,
                entityRequest(null),
                String.class
        )
    }

    def expectedCreatedResource(ResponseEntity response) {
        newValidIncomeSourceResource(
                incomeSourceId: idFromLocation(response.getHeaders().getLocation())
        )
    }

    def expectedCreationFailure() {
        def filePath = 'income/web/income-source-creation-failure-response.json'
        def failureAsMap = new JsonSlurper().parse(new ClassPathResource(filePath).getFile())
        JsonOutput.toJson(failureAsMap)
    }

    def expectUpdateNotFoundFailure() {
        JsonOutput.toJson([
                status   : 404,
                errorCode: 'NOT_FOUND',
                message  : 'Income source not found.'
        ])
    }

    def expectDeleteConflictFailure() {
        JsonOutput.toJson([
                status   : 409,
                errorCode: 'CONFLICT',
                message  : 'Income source is currently in use.'
        ])
    }

    UUID idFromLocation(URI location) {
        def paths = location.getPath().split("/")
        return UUID.fromString(paths[paths.length - 1])
    }

    def entityRequest(String req) {
        var headers = new HttpHeaders()
        headers.setContentType(APPLICATION_JSON)

        new HttpEntity<>(req, headers)
    }

}
