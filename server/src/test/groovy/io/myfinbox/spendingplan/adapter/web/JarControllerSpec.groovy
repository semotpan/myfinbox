package io.myfinbox.spendingplan.adapter.web

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.myfinbox.TestServerApplication
import io.myfinbox.spendingplan.domain.JarExpenseCategories
import io.myfinbox.spendingplan.domain.JarIdentifier
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
import static io.myfinbox.spendingplan.domain.JarExpenseCategory.CategoryIdentifier
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.HttpMethod.PUT
import static org.springframework.http.HttpStatus.*
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

    @Autowired
    JarExpenseCategories jarExpenseCategories

    def cleanup() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, 'spending_jar_expense_category', 'spending_jars', 'spending_plans')
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

    @Sql(['/spendingplan/web/plan-create.sql', '/spendingplan/web/jars-create.sql', '/spendingplan/web/jar_expense_category-create.sql'])
    def "should modify provided category list"() {
        given: 'one plan, one jar with 2 existing categories, user wants to remove existing and add one new'
        def category3Id = UUID.randomUUID()
        var request = newSampleJarCategoriesResource(categories: [
                newSampleJarCategoryToAddAsMap(categoryId: jarCategoryId, toAdd: false),
                newSampleJarCategoryToAddAsMap(categoryId: jarCategoryId2, toAdd: false),
                newSampleJarCategoryToAddAsMap(categoryId: category3Id)
        ])

        when: 'modify categories'
        var response = putJarCategories(request)

        then: 'response has status code ok'
        assert response.getStatusCode() == OK

        and: 'only one category was recorded into the database'
        assert jarExpenseCategories.findByJarId(new JarIdentifier(UUID.fromString(jarId))).size() == 1
        assert jarExpenseCategories.existsByJarIdAndCategoryId(new JarIdentifier(UUID.fromString(jarId)), new CategoryIdentifier(category3Id))
    }

    def "should fail to modify when plan jar not found"() {
        given: 'user wants to modify categories for provided plan jar'
        var request = newSampleJarCategoriesResource(
                categories: [newSampleJarCategoryToAddAsMap(categoryId: jarCategoryId)]
        )

        when: 'modification fails'
        var response = putJarCategories(request)

        then: 'response has status code not found'
        assert response.getStatusCode() == NOT_FOUND

        and: 'response body contains validation failure response'
        JSONAssert.assertEquals(expectedCategoriesModificationFailure(), response.getBody(), LENIENT)
    }

    def postJar(String req) {
        restTemplate.postForEntity("/v1/plans/${planId}/jars", entityRequest(req), String.class)
    }

    def putJarCategories(String req) {
        restTemplate.exchange(
                "/v1/plans/${planId}/jars/${jarId}/expense-categories",
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

    def expectedCategoriesModificationFailure() {
        JsonOutput.toJson([
                status   : 404,
                errorCode: "NOT_FOUND",
                message  : "Spending plan jar was not found."
        ])
    }
}
