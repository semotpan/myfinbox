package io.myfinbox.spendingplan.application;

import io.myfinbox.spendingplan.domain.AccountIdentifier;
import io.myfinbox.spendingplan.domain.Plan;
import io.myfinbox.spendingplan.domain.Plans;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static io.myfinbox.spendingplan.domain.Plan.PlanIdentifier;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class PlanQueryService implements PlanQuery {

    private final Plans plans;

    @Override
    public PlanQueryBuilder search() {
        return new DefaultPlanQueryBuilder(plans);
    }

    @RequiredArgsConstructor
    private static class DefaultPlanQueryBuilder implements PlanQueryBuilder {

        private final Plans plans;

        private UUID planId;
        private UUID accountId;

        @Override
        public PlanQueryBuilder withPlanId(UUID planId) {
            this.planId = planId;
            return this;
        }

        @Override
        public PlanQueryBuilder withAccountId(UUID accountId) {
            this.accountId = accountId;
            return this;
        }

        @Override
        public List<Plan> list() {
            if (nonNull(planId) && nonNull(accountId)) {
                return plans.findByIdAndAccountIdEagerJars(new PlanIdentifier(planId), new AccountIdentifier(accountId))
                        .map(List::of)
                        .orElse(emptyList());
            }

            if (nonNull(accountId)) {
                return plans.findByAccountIdEagerJars(new AccountIdentifier(accountId));
            }

            if (nonNull(planId)) {
                return plans.findByIdEagerJars(new PlanIdentifier(planId))
                        .map(List::of)
                        .orElse(emptyList());
            }

            return emptyList();
        }
    }
}
