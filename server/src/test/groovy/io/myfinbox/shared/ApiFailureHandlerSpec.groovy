package io.myfinbox.shared

import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.shared.ApiErrorResponse.ApiErrorField
import static org.springframework.http.HttpStatus.*

@Tag("unit")
class ApiFailureHandlerSpec extends Specification {

    ApiFailureHandler handler

    def setup() {
        handler = new ApiFailureHandler()
    }

    def "should throw NullPointerException when null failure"() {
        when: 'handle null failure'
        handler.handle(null)

        then: 'NullPointerException exception is thrown'
        def e = thrown(NullPointerException)

        and: 'exception message'
        assert e.getMessage() == 'failure cannot be null'
    }

    def "should get conflict API response when conflict failure"() {
        given: 'a conflict failure'
        def conflict = Failure.ofConflict("Duplicate message")

        when: 'handle conflict failure'
        def response = handler.handle(conflict)

        then: 'response status code is conflict'
        assert response.getStatusCode() == CONFLICT

        and: 'response body contains api response'
        assert response.getBody().status() == 409
        assert response.getBody().message() == "Duplicate message"
        assert response.getBody().errorCode() == CONFLICT
    }

    def "should get not found API response when not fount failure"() {
        given: 'a not found failure'
        def notFound = Failure.ofNotFound("No found resource")

        when: 'handle not found failure'
        def response = handler.handle(notFound)

        then: 'response status code is not found'
        assert response.getStatusCode() == NOT_FOUND

        and: 'response body contains api response'
        assert response.getBody().status() == 404
        assert response.getBody().message() == "No found resource"
        assert response.getBody().errorCode() == NOT_FOUND
    }

    def "should get forbidden API response when action not allowed"() {
        given: 'a forbidden failure'
        def forbidden = Failure.ofForbidden("No allowed to amend resource")

        when: 'handle forbidden failure'
        def response = handler.handle(forbidden)

        then: 'response status code is forbidden'
        assert response.getStatusCode() == FORBIDDEN

        and: 'response body contains api response'
        assert response.getBody().status() == 403
        assert response.getBody().message() == "No allowed to amend resource"
        assert response.getBody().errorCode() == FORBIDDEN
    }

    def "should get unprocessable entity API response when validation failure"() {
        given: 'a validation failure'
        def notFound = Failure.ofValidation("Schema Validation failure", [
                new Failure.FieldViolation("field", "Field cannot be empty", "")
        ])

        when: 'handle validation failure'
        def response = handler.handle(notFound)

        then: 'response status code is unprocessable entity'
        assert response.getStatusCode() == UNPROCESSABLE_ENTITY

        and: 'response body contains api response'
        assert response.getBody().status() == 422
        assert response.getBody().message() == "Schema Validation failure"
        assert response.getBody().errorCode() == UNPROCESSABLE_ENTITY
        assert response.getBody().errors() == [new ApiErrorField("field", "Field cannot be empty", "")]
    }

    def "should throw IllegalArgumentException when not failure handler"() {
        given: 'a non existing failure'
        def nonExisting = new Failure() {}

        when: 'handle nonExisting failure'
        handler.handle(nonExisting)

        then: 'IllegalArgumentException exception is thrown'
        def e = thrown(IllegalArgumentException)

        and: 'exception message'
        assert e.getMessage() == 'No handler found'
    }
}
