package com.demidrolll.myphotos.generator;

import com.demidrolll.myphotos.common.annotation.cdi.Property;
import com.demidrolll.myphotos.common.config.ImageCategory;
import com.demidrolll.myphotos.common.model.TempImageResource;
import com.demidrolll.myphotos.ejb.service.bean.PhotoServiceBean;
import com.demidrolll.myphotos.ejb.service.bean.ProfileServiceBean;
import com.demidrolll.myphotos.ejb.service.bean.UpdateProfileRatingBean;
import com.demidrolll.myphotos.generator.component.AbstractEnvironmentGenerator;
import com.demidrolll.myphotos.generator.component.PhotoGenerator;
import com.demidrolll.myphotos.generator.component.ProfileGenerator;
import com.demidrolll.myphotos.generator.component.UpdatePhotoService;
import com.demidrolll.myphotos.model.domain.Photo;
import com.demidrolll.myphotos.model.domain.Profile;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
public class DataGenerator extends AbstractEnvironmentGenerator {

    @Inject
    private ProfileGenerator profileGenerator;

    @Inject
    private PhotoGenerator photoGenerator;

    @Inject
    private UpdatePhotoService updatePhotoService;

    @EJB
    private ProfileServiceBean profileServiceBean;

    @EJB
    private PhotoServiceBean photoServiceBean;

    @EJB
    private UpdateProfileRatingBean updateProfileRatingBean;

    @Resource(mappedName = "MyPhotosDBPool")
    private DataSource dataSource;

    @Inject
    @Property("myphotos.storage.root.dir")
    private String storageRoot;

    @Inject
    @Property("myphotos.media.absolute.root")
    private String mediaRoot;

    public static void main(String[] args) {
        try {
            new DataGenerator().execute();
        } catch (Throwable exception) {
            log.error("Application error", exception);
        }
    }

    @Override
    protected void generate() throws Exception {
        clearExternalResources();
        List<Profile> profileList = profileGenerator.generateProfiles();
        List<Photo> uploadedPhotos = new ArrayList<>();
        for (Profile profile : profileList) {
            profileServiceBean.signUp(profile, false);
            profileServiceBean.uploadNewAvatar(profile, new PathImageResource(profile.getAvatarUrl()));
            List<String> photoPaths = photoGenerator.generatePhotos(profile.getPhotoCount());
            for (String path : photoPaths) {
                Profile dbProfile = profileServiceBean.findById(profile.getId());
                uploadedPhotos.add(photoServiceBean.uploadNewPhoto(dbProfile, new PathImageResource(path)));
            }
        }
        updatePhotoService.updatePhotos(uploadedPhotos);
        updateProfileRatingBean.updateProfileRating();
        log.info("Generated {} profiles", profileList.size());
        log.info("Generated {} photos", uploadedPhotos.size());
    }

    private void clearExternalResources() throws SQLException, IOException {
        clearDatabase();
        clearDirectory(storageRoot);
        clearDirectory(mediaRoot + ImageCategory.LARGE_PHOTO.getRelativeRoot());
        clearDirectory(mediaRoot + ImageCategory.SMALL_PHOTO.getRelativeRoot());
        clearDirectory(mediaRoot + ImageCategory.PROFILE_AVATAR.getRelativeRoot());
    }

    private void clearDirectory(String storageRoot) throws IOException {
        Path path = Path.of(storageRoot);
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

            });
            log.info("Directory {} cleared", path);
        } else {
            Files.createDirectories(path);
        }
    }

    private void clearDatabase() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("TRUNCATE photo CASCADE");
            statement.executeUpdate("TRUNCATE access_token CASCADE");
            statement.executeUpdate("TRUNCATE profile CASCADE");
            statement.execute("SELECT SETVAL('profile_seq', 1, false)");
            statement.execute("SELECT SETVAL('photo_seq', 123456, false)");
        }
        log.info("Database cleared");
    }

    private static class PathImageResource extends TempImageResource {

        public PathImageResource(String path) throws IOException {
            Files.copy(Paths.get(path), getTempPath(), REPLACE_EXISTING);
        }
    }
}
