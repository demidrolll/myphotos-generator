package com.demidrolll.myphotos.generator.alternative;

import com.demidrolll.myphotos.ejb.repository.jpa.StaticJpaQueryInitializer;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.Bean;

@Dependent
@TestDataGeneratorEnvironment
public class JpaRepositoryFinderAlternative extends StaticJpaQueryInitializer.JpaRepositoryFinder {

    @Override
    protected boolean isCandidateValid(Bean<?> bean) {
        return false;
    }
}
