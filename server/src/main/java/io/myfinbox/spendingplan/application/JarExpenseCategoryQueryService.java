package io.myfinbox.spendingplan.application;

import io.myfinbox.spendingplan.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class JarExpenseCategoryQueryService implements JarExpenseCategoryQuery {

    private final JarExpenseCategories jarExpenseCategories;
    private final Jars jars;

    @Override
    public List<JarExpenseCategory> search(UUID planId, UUID jarId) {
        if (isNull(planId) || isNull(jarId)) {
            return emptyList();
        }

        if (!jars.existsByIdAndPlan(new Plan.PlanIdentifier(planId), new JarIdentifier(jarId))) {
            return emptyList();
        }

        return jarExpenseCategories.findByJarId(new JarIdentifier(jarId));
    }
}
