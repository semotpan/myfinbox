package io.myfinbox.income.adapter.web

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.myfinbox.TestServerApplication
import io.myfinbox.income.DataSamples
import io.myfinbox.income.IncomeCreated
import io.myfinbox.income.IncomeUpdated
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

import static io.myfinbox.income.DataSamples.*
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.HttpMethod.PUT
import static org.springframework.http.HttpStatus.*
import static org.springframework.http.MediaType.APPLICATION_JSON

@Tag("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestServerApplication)
@ApplicationModuleTest
@TestPropertySource(locations = "/application-test.properties")
class IncomeControllerSpec extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    PublishedEvents events

    @Autowired
    TestRestTemplate restTemplate

    def cleanup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, 'incomes', 'incomesource')
    }

    @Sql('/income/web/incomesource-create.sql')
    def "should create a new income"() {
        given: 'user wants to create a new income'
        var request = newValidIncomeResource()

        when: 'income is created'
        var response = postIncome(request)

        then: 'response status is created'
        assert response.getStatusCode() == CREATED

        and: 'location header contains the created income URL location'
        assert response.getHeaders().getLocation() != null

        and: 'body contains created resource'
        JSONAssert.assertEquals(expectedCreatedResource(response), response.getBody(), LENIENT)

        and: 'income created event raised'
        assert events.ofType(IncomeCreated.class).contains(
                newValidIncomeCreatedEvent(incomeId: idFromLocation(response.getHeaders().getLocation()))
        )
    }

    def "should fail creation when request has validation failures"() {
        given: 'user wants to create a new income'
        var request = '{}'

        when: 'income fails to create'
        var response = postIncome(request)

        then: 'response has status code unprocessable entity'
        assert response.getStatusCode() == UNPROCESSABLE_ENTITY

        and: 'response body contains validation failure response'
        JSONAssert.assertEquals(expectedCreationFailure(), response.getBody(), LENIENT)
    }


    @Sql(['/income/web/incomesource-create.sql', '/income/web/income-create.sql'])
    def "should update an income"() {
        given: 'user wants to update an existing income'
        var request = newValidIncomeResource(
                incomeSourceId: incomeSourceId2,
                paymentType: "Card",
                amount: 50,
                currencyCode: "MDL",
                incomeDate: '2024-03-19',
                description: 'Other sources'
        )

        when: 'income is updated'
        var response = putIncome(request)

        then: 'response status is ok'
        assert response.getStatusCode() == OK

        and: 'body contains updated resource'
        JSONAssert.assertEquals(expectedUpdatedResource(), response.getBody(), LENIENT)

        and: 'income updated event raised'
        assert events.ofType(IncomeUpdated.class).contains(
                newValidIncomeUpdatedEvent(
                        incomeSourceId: incomeSourceId2,
                        paymentType: "CARD",
                        amount: [
                                amount  : 50,
                                currency: "MDL"
                        ],
                        incomeDate: '2024-03-19'
                )
        )
    }

    @Sql(['/income/web/incomesource-create.sql', '/income/web/income-create.sql'])
    def "should fail updating when provided income source not found"() {
        given: 'user wants to update an existing income'
        var request = newValidIncomeResource(
                incomeSourceId: UUID.randomUUID()
        )

        when: 'income fails to update'
        var response = putIncome(request)

        then: 'response has status code not found'
        assert response.getStatusCode() == NOT_FOUND

        and: 'response body contains not found failure response'
        JSONAssert.assertEquals(expectedUpdateFailure(), response.getBody(), LENIENT)
    }

    def postIncome(String req) {
        restTemplate.postForEntity('/v1/incomes', entityRequest(req), String.class)
    }

    def putIncome(String req) {
        restTemplate.exchange(
                "/v1/incomes/${DataSamples.incomeId}",
                PUT,
                entityRequest(req),
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
        newValidIncomeResource(
                incomeId: idFromLocation(response.getHeaders().getLocation())
        )
    }

    def expectedUpdatedResource() {
        newValidIncomeResource(
                incomeSourceId: incomeSourceId2,
                paymentType: "Card",
                amount: 50,
                currencyCode: "MDL",
                incomeDate: '2024-03-19',
                description: 'Other sources'
        )
    }

    def expectedCreationFailure() {
        def filePath = 'income/web/income-creation-failure-response.json'
        def failureAsMap = new JsonSlurper().parse(new ClassPathResource(filePath).getFile())
        JsonOutput.toJson(failureAsMap)
    }

    def expectedUpdateFailure() {
        JsonOutput.toJson([
                status   : 404,
                errorCode: "NOT_FOUND",
                message  : "Income source not found for the provided account."
        ])
    }
}
