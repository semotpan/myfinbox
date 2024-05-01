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
import org.springframework.test.jdbc.JdbcTestUtils
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.spendingplan.DataSamples.newSampleClassicCreatePlanResource
import static io.myfinbox.spendingplan.DataSamples.newSampleCreatePlanResource
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
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

    def "should create a new spending plan"() {
        given: 'user wants to create a spending plan'
        var request = newSampleCreatePlanResource()

        when: 'plan is created'
        var response = postPlan(request)

        then: 'response status is created'
        assert response.getStatusCode() == CREATED

        and: 'location header contains the created spending plan URL location'
        assert response.getHeaders().getLocation() != null

        and: 'body contains created resource'
        JSONAssert.assertEquals(expectedCreatedResource(response), response.getBody(), LENIENT)
    }

    def "should fail creation when request has validation failures"() {
        given: 'user wants to create a new spending plan'
        var request = '{}'

        when: 'expense fails to create'
        var response = postPlan(request)

        then: 'response has status code unprocessable entity'
        assert response.getStatusCode() == UNPROCESSABLE_ENTITY

        and: 'response body contains validation failure response'
        JSONAssert.assertEquals(expectedCreationFailure(), response.getBody(), LENIENT)
    }

    def "should create a new classic spending plan"() {
        given: 'user wants to create a classic spending plan'
        var request = newSampleClassicCreatePlanResource()

        when: 'classic plan is created'
        var response = postClassicPlan(request)

        then: 'response status is created'
        assert response.getStatusCode() == CREATED

        and: 'location header contains the created classic spending plan URL location'
        assert response.getHeaders().getLocation() != null

        and: 'body contains created resource'
        JSONAssert.assertEquals(expectedClassicCreatedResource(response), response.getBody(), LENIENT)

        and: 'six jars were created'
        assert jars.findAll().size() == 6
    }

    def "should fail classic plan creation when request has validation failures"() {
        given: 'user wants to create a new spending plan'
        var request = '{}'

        when: 'expense fails to create'
        var response = postClassicPlan(request)

        then: 'response has status code unprocessable entity'
        assert response.getStatusCode() == UNPROCESSABLE_ENTITY

        and: 'response body contains validation failure response'
        JSONAssert.assertEquals(expectedClassicCreationFailure(), response.getBody(), LENIENT)
    }

    def postPlan(String req) {
        restTemplate.postForEntity('/v1/plans', entityRequest(req), String.class)
    }

    def postClassicPlan(String req) {
        restTemplate.postForEntity('/v1/plans/classic', entityRequest(req), String.class)
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

    def expectedClassicCreationFailure() {
        def filePath = 'spendingplan/web/classic-plan-creation-failure-response.json'
        def failureAsMap = new JsonSlurper().parse(new ClassPathResource(filePath).getFile())
        JsonOutput.toJson(failureAsMap)
    }
}
