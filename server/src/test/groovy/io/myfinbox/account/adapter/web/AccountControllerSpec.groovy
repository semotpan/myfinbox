package io.myfinbox.account.adapter.web

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.myfinbox.TestServerApplication
import io.myfinbox.account.AccountCreated
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.modulith.test.ApplicationModuleTest
import org.springframework.modulith.test.PublishedEvents
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.account.DataSamples.ACCOUNT_RESOURCE
import static io.myfinbox.account.DataSamples.newSampleAccountEvent
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.HttpStatus.*
import static org.springframework.http.MediaType.APPLICATION_JSON

@Tag("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestServerApplication)
@ApplicationModuleTest
@TestPropertySource(locations = "/application-test.properties")
class AccountControllerSpec extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    PublishedEvents events

    @Autowired
    TestRestTemplate restTemplate

    def cleanup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, 'accounts')
    }

    def "should create an new account with default locale"() {
        given: 'user wants to create an account'
        var request = JsonOutput.toJson(ACCOUNT_RESOURCE)

        when: 'account is created'
        var response = postNewAccount(request)

        then: 'response has status code created'
        assert response.getStatusCode() == CREATED

        and: 'location header contains the create account URL location'
        assert response.getHeaders().getLocation() != null

        and: 'body contains created resource'
        JSONAssert.assertEquals(accountResource(idFromLocation(response.getHeaders().getLocation())), response.getBody(), LENIENT)

        and: 'account created event raised'
        assert events.ofType(AccountCreated.class).contains(
                newSampleAccountEvent(accountId: idFromLocation(response.getHeaders().getLocation()))
        )
    }

    def "should fail to create an account when request has validation failures"() {
        given: 'a request with invalid data'
        var request = "{}"

        when: 'account failed to create'
        var response = postNewAccount(request)

        then: 'response has status code unprocessable entity'
        assert response.getStatusCode() == UNPROCESSABLE_ENTITY

        and: 'response body contains validation failure response'
        JSONAssert.assertEquals(expectedCreationFailure(), response.getBody(), LENIENT)
    }

    @Sql('/account/web/create-account.sql')
    def "should gen one account when exits"() {
        given: 'an exiting account'
        var accountId = UUID.fromString("e2709aa2-7907-4f78-98b6-0f36a0c1b5ca")

        when: 'account is resource is received'
        var response = getOneAccount(accountId)

        then: 'response has status code OK'
        assert response.getStatusCode() == OK

        and: 'response body contains exiting account'
        JSONAssert.assertEquals(JsonOutput.toJson(ACCOUNT_RESOURCE), response.getBody(), LENIENT)
    }

    def "should get not found when account doesn't exit"() {
        given: 'a non exiting account'
        var accountId = UUID.fromString("e2709aa2-7907-4f78-98b6-0f36a0c1b5ca")

        when: 'account is not found'
        var response = getOneAccount(accountId)

        then: 'response has status code NOT_FOUND'
        assert response.getStatusCode() == NOT_FOUND

        and: 'response body contains not found failure'
        JSONAssert.assertEquals(expectAccountNotFoundFailure(accountId), response.getBody(), LENIENT)
    }

    def postNewAccount(String req) {
        restTemplate.postForEntity('/v1/accounts', entityRequest(req), String.class)
    }

    def getOneAccount(UUID accountId) {
        def uri = UriComponentsBuilder.fromUriString("${restTemplate.getRootUri()}/v1/accounts/${accountId}")
                .build()
                .toUri()

        restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                String.class
        )
    }

    def entityRequest(String req) {
        var headers = new HttpHeaders()
        headers.setContentType(APPLICATION_JSON)

        new HttpEntity<>(req, headers)
    }

    def expectedCreationFailure() {
        def filePath = 'account/web/account-creation-failure-response.json'
        def failureAsMap = new JsonSlurper().parse(new ClassPathResource(filePath).getFile())
        JsonOutput.toJson(failureAsMap)
    }

    UUID idFromLocation(URI location) {
        def paths = location.getPath().split("/")
        return UUID.fromString(paths[paths.length - 1])
    }

    def accountResource(UUID id) {
        JsonOutput.toJson(ACCOUNT_RESOURCE + [accountId: id.toString()])
    }

    def expectAccountNotFoundFailure(UUID accountId) {
        JsonOutput.toJson([
                status   : 404,
                errorCode: 'NOT_FOUND',
                message  : "Account '${accountId}' not found."
        ])
    }
}
