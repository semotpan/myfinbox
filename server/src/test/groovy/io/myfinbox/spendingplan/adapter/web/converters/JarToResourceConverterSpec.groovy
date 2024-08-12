package io.myfinbox.spendingplan.adapter.web.converters

import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.spendingplan.DataSamples.*

@Tag("unit")
class JarToResourceConverterSpec extends Specification {

    JarToResourceConverter converter

    def setup() {
        converter = new JarToResourceConverter();
    }

    def "should convert jar to resource"() {
        when: 'converting a jar'
        def resource = converter.convert(newSampleJar())

        then: 'resource is build as expected'
        assert resource.getJarId() == UUID.fromString(jarId)
        assert resource.getCreationTimestamp() == timestamp
        assert resource.getAmountToReach() == BigDecimal.valueOf(550)
        assert resource.getCurrencyCode() == currency
        assert resource.getPercentage() == 55
        assert resource.getName() == jarName
        assert resource.getDescription() == jarDescription
    }
}
