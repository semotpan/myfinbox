package io.myfinbox

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import spock.lang.Tag

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@Tag("integration")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestServerApplication)
@TestPropertySource(locations = "/application-test.properties")
class ServerApplicationSpec extends Specification {

    @Autowired
    ApplicationContext applicationContext

    def "should load contexts"() {
        expect:
        assert applicationContext != null
    }
}
