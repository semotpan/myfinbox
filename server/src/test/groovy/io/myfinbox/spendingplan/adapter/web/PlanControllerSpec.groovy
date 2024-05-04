package io.myfinbox.spendingplan.adapter.web

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.myfinbox.TestServerApplication
import io.myfinbox.spendingplan.domain.Jars
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
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.jdbc.JdbcTestUtils
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.spendingplan.DataSamples.*
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.HttpMethod.PUT
import static org.springframework.http.HttpStatus.*
import static org.springframework.http.MediaType.APPLICATION_JSON

@Tag("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestServerApplication)
@ApplicationModuleTest
@TestPropertySource(locations = "/application-test.properties")
class PlanControllerSpec extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    TestRestTemplate restTemplate

    @Autowired
    Jars jars

    def cleanup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, 'spending_jars', 'spending_plans')
    }

    def "should successfully create a new spending plan"() {
        given: 'a user wants to create a spending plan'
        def request = newSampleCreatePlanResource()

        when: 'the spending plan is created'
        def response = postPlan(request)

        then: 'the response status is "Created"'
        assert response.getStatusCode() == CREATED

        and: 'the location header contains the URL of the created spending plan'
        assert response.getHeaders().getLocation() != null

        and: 'the response body matches the expected created resource'
        def expectedResource = expectedCreatedResource(response)
        JSONAssert.assertEquals(expectedResource, response.getBody(), LENIENT)
    }

    def "should fail creation when request has validation failures"() {
        given: 'a user wants to create a new spending plan'
        def request = '{}'

        when: 'the plan creation fails'
        def response = postPlan(request)

        then: 'the response has status code Unprocessable Entity'
        assert response.getStatusCode() == UNPROCESSABLE_ENTITY

        and: 'the response body contains validation failure details'
        def expectedFailure = expectedCreationFailure()
        JSONAssert.assertEquals(expectedFailure, response.getBody(), LENIENT)
    }

    def "should create a new classic spending plan"() {
        given: 'a user wants to create a classic spending plan'
        def request = newSampleClassicCreatePlanResource()

        when: 'the classic plan is created'
        def response = postClassicPlan(request)

        then: 'the response status is "Created"'
        assert response.getStatusCode() == CREATED

        and: 'the location header contains the URL of the created classic spending plan'
        assert response.getHeaders().getLocation() != null

        and: 'the response body matches the expected created classic resource'
        def expectedResource = expectedClassicCreatedResource(response)
        JSONAssert.assertEquals(expectedResource, response.getBody(), LENIENT)

        and: 'six jars are created'
        assert jars.findAll().size() == 6
    }

    def "should fail classic plan creation when request has validation failures"() {
        given: 'a user wants to create a new classic spending plan'
        def request = '{}'

        when: 'the classic plan creation fails'
        def response = postClassicPlan(request)

        then: 'the response has status code Unprocessable Entity'
        assert response.getStatusCode() == UNPROCESSABLE_ENTITY

        and: 'the response body contains validation failure details'
        def expectedFailure = expectedClassicCreationFailure()
        JSONAssert.assertEquals(expectedFailure, response.getBody(), LENIENT)
    }

    @Sql(['/spendingplan/web/plan-create.sql', '/spendingplan/web/jars-create.sql'])
    def "should update an existing spending plan"() {
        given: 'a user wants to update an existing spending plan'
        def request = newSampleCreatePlanResource(
                name: 'Update Name',
                amount: 10000,
                currencyCode: 'MDL',
                description: "My personal MDL plan",
        )

        when: 'the plan is updated'
        def response = putPlan(request)

        then: 'the response status is "OK"'
        assert response.getStatusCode() == OK

        and: 'the response body contains the updated resource'
        JSONAssert.assertEquals(expectedUpdatedResource(), response.getBody(), LENIENT)
    }

    def "should fail updating when request has validation failures"() {
        given: 'a user wants to update an existing spending plan'
        def invalidRequest = '{}'

        when: 'the spending plan update fails'
        def response = putPlan(invalidRequest)

        then: 'the response has status code Unprocessable Entity'
        assert response.getStatusCode() == UNPROCESSABLE_ENTITY

        and: 'the response body contains validation failure details'
        def expectedFailure = expectedUpdatingFailure()
        JSONAssert.assertEquals(expectedFailure, response.getBody(), LENIENT)
    }

    def postPlan(String req) {
        restTemplate.postForEntity('/v1/plans', entityRequest(req), String.class)
    }

    def postClassicPlan(String req) {
        restTemplate.postForEntity('/v1/plans/classic', entityRequest(req), String.class)
    }

    def putPlan(String req) {
        restTemplate.exchange(
                "/v1/plans/${planId}",
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
        newSampleCreatePlanResource(
                planId: idFromLocation(response.getHeaders().getLocation())
        )
    }

    def expectedUpdatedResource() {
        newSampleCreatePlanResource(
                name: 'Update Name',
                amount: 10000,
                currencyCode: 'MDL',
                description: "My personal MDL plan",
        )
    }

    def expectedClassicCreatedResource(ResponseEntity response) {
        newSampleCreatePlanResource(
                planId: idFromLocation(response.getHeaders().getLocation()),
                name: 'My classic spending plan',
                description: 'My classic plan distribution: Necessities(55%), Long Term Savings(10%), Education(10%), Play(10%), Financial(10%), Give(5%).'
        )
    }

    def expectedCreationFailure() {
        def filePath = 'spendingplan/web/plan-creation-failure-response.json'
        def failureAsMap = new JsonSlurper().parse(new ClassPathResource(filePath).getFile())
        JsonOutput.toJson(failureAsMap)
    }

    def expectedUpdatingFailure() {
        def filePath = 'spendingplan/web/plan-update-failure-response.json'
        def failureAsMap = new JsonSlurper().parse(new ClassPathResource(filePath).getFile())
        JsonOutput.toJson(failureAsMap)
    }

    def expectedClassicCreationFailure() {
        def filePath = 'spendingplan/web/classic-plan-creation-failure-response.json'
        def failureAsMap = new JsonSlurper().parse(new ClassPathResource(filePath).getFile())
        JsonOutput.toJson(failureAsMap)
    }
}
