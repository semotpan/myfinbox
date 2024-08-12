package io.myfinbox.spendingplan.adapter.web.converters;

import io.myfinbox.rest.JarResource;
import io.myfinbox.spendingplan.domain.Jar;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
final class JarToResourceConverter implements Converter<Jar, JarResource> {

    @Override
    public JarResource convert(Jar jar) {
        return new JarResource()
                .jarId(jar.getId().id())
                .creationTimestamp(jar.getCreationTimestamp().toString())
                .amountToReach(jar.getAmountToReachAsNumber())
                .currencyCode(jar.getCurrencyCode())
                .name(jar.getName())
                .percentage(jar.getPercentage().value())
                .description(jar.getDescription());
    }
}
