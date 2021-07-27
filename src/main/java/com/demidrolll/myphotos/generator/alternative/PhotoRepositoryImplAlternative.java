package com.demidrolll.myphotos.generator.alternative;

import com.demidrolll.myphotos.ejb.repository.jpa.PhotoRepositoryImpl;
import jakarta.enterprise.context.Dependent;

@Dependent
@TestDataGeneratorEnvironment
public class PhotoRepositoryImplAlternative extends PhotoRepositoryImpl {

    @Override
    public int countProfilePhotos(Long profileId) {
        return em
                .createQuery("select count(ph) from Photo ph where ph.profile.id = :profileId", Integer.class)
                .setParameter("profileId", profileId)
                .getSingleResult();
    }
}
