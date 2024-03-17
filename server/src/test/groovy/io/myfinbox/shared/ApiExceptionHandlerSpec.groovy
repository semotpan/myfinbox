package io.myfinbox.shared

import groovy.json.JsonOutput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.ResponseEntity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.bind.annotation.*
import spock.lang.Specification
import spock.lang.Tag

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.matchesPattern
import static org.springframework.http.HttpHeaders.ALLOW
import static org.springframework.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.HttpMethod.GET
import static org.springframework.http.MediaType.*
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@Tag("integration")
@WebMvcTest(controllers = [Config.TestResource])
@Import([ApiExceptionHandler])
class ApiExceptionHandlerSpec extends Specification {

    static ISO_8601 = "^\\d{4}-[01]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-5]\\d\\.\\d{3,}([+-][0-2]\\d(:?[0-5]\\d)?|Z)\$"

    @Autowired
    MockMvc mockMvc

    def "should get Bad Request when message not readable exception"() {
        var lenientResp = JsonOutput.toJson([
                status   : 400,
                errorCode: "BAD_REQUEST",
                message  : "Malformed JSON request"
        ])

        expect:
        mockMvc.perform(post('/test-url2')
                .contentType(APPLICATION_JSON)
                .content('{'))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(lenientResp))
                .andExpect(jsonPath('$.timestamp').value(matchesPattern(ISO_8601)))
    }

    def "should get Bad Request when type mismatch exception"() {
        var lenientResp = JsonOutput.toJson([
                status   : 400,
                errorCode: "BAD_REQUEST",
                message  : "Type mismatch request"
        ])

        expect:
        mockMvc.perform(post('/test-url3/1234')
                .contentType(APPLICATION_JSON)
                .content('{'))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(lenientResp))
                .andExpect(jsonPath('$.timestamp').value(matchesPattern(ISO_8601)))
    }

    def "should get Not Found when no handler found exception"() {
        var lenientResp = JsonOutput.toJson([
                status   : 404,
                errorCode: "NOT_FOUND",
                message  : "Resource '/unknown' not found"
        ])

        expect:
        mockMvc.perform(get('/unknown'))
                .andExpect(status().isNotFound())
                .andExpect(header().string(CONTENT_TYPE, is(APPLICATION_JSON_VALUE)))
                .andExpect(content().json(lenientResp))
                .andExpect(jsonPath('$.timestamp').value(matchesPattern(ISO_8601)))
    }

    def "should get Method Not Allowed when request method not supported exception"() {
        var lenientResp = JsonOutput.toJson([
                status   : 405,
                errorCode: "METHOD_NOT_ALLOWED",
                message  : "Request method 'POST' is not supported"
        ])

        expect:
        mockMvc.perform(post('/test-url'))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(CONTENT_TYPE, is(APPLICATION_JSON_VALUE)))
                .andExpect(header().string(ALLOW, is(GET.name())))
                .andExpect(content().json(lenientResp))
                .andExpect(jsonPath('$.timestamp').value(matchesPattern(ISO_8601)))
    }

    def "should get Not Acceptable when media type not acceptable exception"() {
        var lenientResp = JsonOutput.toJson([
                status   : 406,
                errorCode: "NOT_ACCEPTABLE",
                message  : "Could not find acceptable representation"
        ])

        expect:
        mockMvc.perform(get('/test-url')
                .accept(APPLICATION_JSON))
                .andExpect(status().isNotAcceptable())
                .andExpect(header().string(CONTENT_TYPE, is(APPLICATION_JSON_VALUE)))
                .andExpect(content().json(lenientResp))
                .andExpect(jsonPath('$.timestamp').value(matchesPattern(ISO_8601)))
    }

    def "should get Unsupported media type when media type not supported exception"() {
        var lenientResp = JsonOutput.toJson([
                status   : 415,
                errorCode: "UNSUPPORTED_MEDIA_TYPE",
                message  : "Content type 'application/xml' is not supported",
        ])

        expect:
        mockMvc.perform(post("/test-url3/${UUID.randomUUID()}")
                .contentType(APPLICATION_XML))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(header().string(CONTENT_TYPE, is(APPLICATION_JSON_VALUE)))
                .andExpect(content().json(lenientResp))
                .andExpect(jsonPath('$.timestamp').value(matchesPattern(ISO_8601)))
    }

    def "should get Internal Server Error when unexpected exception thrown"() {
        var lenientResp = JsonOutput.toJson([
                status   : 500,
                errorCode: "INTERNAL_SERVER_ERROR",
                message  : "An unexpected error occurred"
        ])

        expect:
        mockMvc.perform(post("/test-url3/${UUID.randomUUID()}")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string(CONTENT_TYPE, is(APPLICATION_JSON_VALUE)))
                .andExpect(content().json(lenientResp))
                .andExpect(jsonPath('$.timestamp').value(matchesPattern(ISO_8601)))
    }

    @TestConfiguration
    static class Config {

        @RestController
        static class TestResource {

            @GetMapping(path = '/test-url', produces = TEXT_HTML_VALUE)
            String get() {
                'test'
            }

            @GetMapping(path = '/test-url2', produces = APPLICATION_JSON_VALUE)
            ResponseEntity<?> getQuery(Request request) {
                ResponseEntity.ok("{}")
            }

            @PostMapping(path = '/test-url2', consumes = APPLICATION_JSON_VALUE)
            void post(@RequestBody Request request) {
                throw new UnsupportedOperationException('Not implemented')
            }

            @PostMapping(path = '/test-url3/{id}', consumes = APPLICATION_JSON_VALUE)
            void post(@PathVariable(name = "id") UUID id) {
                throw new UnsupportedOperationException('Not implemented')
            }
        }

        record Request(Integer value, String value2) {}
    }
}
