package io.myfinbox.spendingplan.application;

import io.myfinbox.spendingplan.domain.Jar;
import io.myfinbox.spendingplan.domain.JarIdentifier;
import io.myfinbox.spendingplan.domain.Jars;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static io.myfinbox.spendingplan.domain.Plan.*;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class JarQueryService implements JarQuery {

    private final Jars jars;

    @Override
    public JarQueryBuilder search() {
        return new DefaultJarQueryBuilder(jars);
    }

    @RequiredArgsConstructor
    private static final class DefaultJarQueryBuilder implements JarQueryBuilder {

        private final Jars jars;

        private UUID planId;
        private UUID jarId;

        @Override
        public JarQueryBuilder withPlanId(UUID planId) {
            this.planId = planId;
            return this;
        }

        @Override
        public JarQueryBuilder withJarId(UUID jarId) {
            this.jarId = jarId;
            return this;
        }

        @Override
        public List<Jar> list() {
            if (nonNull(planId) && nonNull(jarId)) {
                return jars.findByIdAndPlanId(new JarIdentifier(jarId), new PlanIdentifier(planId))
                        .map(List::of)
                        .orElse(emptyList());
            }

            if (nonNull(jarId)) {
                return jars.findById(new JarIdentifier(jarId))
                        .map(List::of)
                        .orElse(emptyList());
            }

            if (nonNull(planId)) {
                return jars.findByPlanId(new PlanIdentifier(planId));
            }

            return emptyList();
        }
    }
}
