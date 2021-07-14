package com.demidrolll.myphotos.generator.component;

import com.demidrolll.myphotos.ejb.repository.PhotoRepository;
import com.demidrolll.myphotos.model.domain.Photo;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class UpdatePhotoService {

    @Inject
    private PhotoRepository photoRepository;

    private Random random = new Random();

    @Transactional
    public void updatePhotos(List<Photo> photoList) {
        for (Photo photo : photoList) {
            photo.setDownloads(random.nextInt(100));
            photo.setViews(random.nextInt(1000) * 5 + 100L);
            photoRepository.update(photo);
        }
    }
}
