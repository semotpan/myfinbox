package io.myfinbox.spendingplan.adapter.web

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.myfinbox.TestServerApplication
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
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import static org.springframework.http.MediaType.APPLICATION_JSON

@Tag("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestServerApplication)
@ApplicationModuleTest
@TestPropertySource(locations = "/application-test.properties")
class JarControllerSpec extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    TestRestTemplate restTemplate

    def cleanup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, 'spendingjars', 'spendingplans')
    }

    @Sql('/spendingplan/web/plan-create.sql')
    def "should create a new jar for an existing spending plan"() {
        given: 'user wants to create a jar'
        var request = newSampleCreateJarResource()

        when: 'jar is created'
        var response = postJar(request)

        then: 'response status is created'
        assert response.getStatusCode() == CREATED

        and: 'location header contains the created jar URL location'
        assert response.getHeaders().getLocation() != null

        and: 'body contains created resource'
        JSONAssert.assertEquals(expectedCreatedResource(response), response.getBody(), LENIENT)
    }

    def "should fail creation when request has validation failures"() {
        given: 'user wants to create a jar'
        var request = JsonOutput.toJson([
                percentage: 101
        ])

        when: 'jar fails to create'
        var response = postJar(request)

        then: 'response has status code unprocessable entity'
        assert response.getStatusCode() == UNPROCESSABLE_ENTITY

        and: 'response body contains validation failure response'
        JSONAssert.assertEquals(expectedCreationSchemaValidationFailure(), response.getBody(), LENIENT)
    }

    @Sql(['/spendingplan/web/plan-create.sql', '/spendingplan/web/jars-create.sql'])
    def "should fail creation when request resource jar percentage is greater than allowed"() {
        given: 'user wants to create a jar'
        var request = newSampleCreateJarResource(
                name: 'Finances'
        )

        when: 'jar fails to create'
        var response = postJar(request)

        then: 'response has status code unprocessable entity'
        assert response.getStatusCode() == UNPROCESSABLE_ENTITY

        and: 'response body contains validation failure response'
        JSONAssert.assertEquals(expectedCreationPercentageValidationFailure(), response.getBody(), LENIENT)
    }

    def postJar(String req) {
        restTemplate.postForEntity("/v1/plans/${planId}/jars", entityRequest(req), String.class)
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
        newSampleCreateJarResource(
                jarId: idFromLocation(response.getHeaders().getLocation()),
                amountToReach: 550,
                currencyCode: currency
        )
    }

    def expectedCreationSchemaValidationFailure() {
        def filePath = 'spendingplan/web/jar-creation-schema-failure-response.json'
        def failureAsMap = new JsonSlurper().parse(new ClassPathResource(filePath).getFile())
        JsonOutput.toJson(failureAsMap)
    }

    def expectedCreationPercentageValidationFailure() {
        def filePath = 'spendingplan/web/jar-creation-invalid-perc-failure-response.json'
        def failureAsMap = new JsonSlurper().parse(new ClassPathResource(filePath).getFile())
        JsonOutput.toJson(failureAsMap)
    }
}
