package io.myfinbox.spendingplan.domain;

import io.myfinbox.spendingplan.domain.Jar.JarIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Jars extends JpaRepository<Jar, JarIdentifier> {

    boolean existsByNameAndPlan(String name, Plan plan);

}
