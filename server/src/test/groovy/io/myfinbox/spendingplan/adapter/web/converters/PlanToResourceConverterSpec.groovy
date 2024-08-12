package io.myfinbox.spendingplan.adapter.web.converters

import io.myfinbox.rest.JarResource
import io.myfinbox.spendingplan.domain.Jar
import org.springframework.core.convert.converter.Converter
import spock.lang.Specification
import spock.lang.Tag

import static io.myfinbox.spendingplan.DataSamples.*

@Tag("unit")
class PlanToResourceConverterSpec extends Specification {

    Converter<Jar, JarResource> jarResourceConverter
    PlanToResourceConverter converter

    def setup() {
        jarResourceConverter = Mock()
        converter = new PlanToResourceConverter(jarResourceConverter)
    }

    def "should convert plan to resource"() {
        setup: 'jar converter get a sample jar resource'
        1 * jarResourceConverter.convert(_ as Jar) >> new JarResource()

        when: 'converting a plan'
        def samplePlan = newSamplePlanBuilder();

        def resource = converter.convert(samplePlan.build())

        then: 'resource is build a expected'
        assert resource.getAccountId() == UUID.fromString(accountId)
        assert resource.getPlanId() != null
        assert resource.getName() == name
        assert resource.getCreationTimestamp() != null
        assert resource.getAmount() == amount
        assert resource.getCurrencyCode() == currency
        assert resource.getDescription() == planDescription
        assert resource.getJars().size() == 1
    }
}
