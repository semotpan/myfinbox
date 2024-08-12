package io.myfinbox.spendingplan.adapter.web.converters;

import io.myfinbox.rest.JarResource;
import io.myfinbox.rest.PlanResource;
import io.myfinbox.spendingplan.domain.Jar;
import io.myfinbox.spendingplan.domain.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
final class PlanToResourceConverter implements Converter<Plan, PlanResource> {

    private final Converter<Jar, JarResource> jarResourceConverter;

    @Override
    public PlanResource convert(Plan plan) {
        return new PlanResource()
                .planId(plan.getId().id())
                .name(plan.getName())
                .creationTimestamp(plan.getCreationTimestamp().toString())
                .amount(plan.getAmountAsNumber())
                .currencyCode(plan.getCurrencyCode())
                .accountId(plan.getAccount().id())
                .description(plan.getDescription())
                .jars(plan.getJars().stream()
                        .map(jarResourceConverter::convert)
                        .toList());
    }
}
