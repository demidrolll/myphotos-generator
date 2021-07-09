package com.demidrolll.myphotos.generator;

import com.demidrolll.myphotos.common.annotation.cdi.Property;
import com.demidrolll.myphotos.common.config.ImageCategory;
import com.demidrolll.myphotos.generator.component.AbstractEnvironmentGenerator;
import com.demidrolll.myphotos.generator.component.PhotoGenerator;
import com.demidrolll.myphotos.generator.component.ProfileGenerator;
import com.demidrolll.myphotos.model.domain.Profile;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Slf4j
public class DataGenerator extends AbstractEnvironmentGenerator {

    @Inject
    private ProfileGenerator profileGenerator;

    @Inject
    private PhotoGenerator photoGenerator;

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
        } catch (Exception exception) {
            log.error("Application error", exception);
        }
    }

    @Override
    protected void generate() throws Exception {
        clearExternalResources();
        List<Profile> profileList = profileGenerator.generateProfiles();
        // TODO create profiles
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
}
