package io.myfinbox

import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter
import spock.lang.Specification
import spock.lang.Tag

@Tag("unit")
class ModularitySpec extends Specification {

    ApplicationModules modules = ApplicationModules.of(ServerApplication.class)

    def "should verify modular structure"() {
        expect:
        assert modules.verify()
    }

    def "should write documentation snippets"() {
        expect:
        new Documenter(modules)
                .writeDocumentation()
                .writeModulesAsPlantUml()
    }
}
