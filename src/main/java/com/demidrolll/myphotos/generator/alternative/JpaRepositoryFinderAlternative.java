package com.demidrolll.myphotos.generator.alternative;

import com.demidrolll.myphotos.ejb.repository.jpa.StaticJpaQueryInitializer;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.Bean;

@Dependent
@TestDataGeneratorEnvironment
public class JpaRepositoryFinderAlternative extends StaticJpaQueryInitializer.JpaRepositoryFinder {

    @Override
    protected boolean isCandidateValid(Bean<?> bean) {
        return false;
    }
}
