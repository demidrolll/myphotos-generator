package com.demidrolll.myphotos.generator.component;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@ApplicationScoped
@Slf4j
public class PhotoGenerator {

    private final Random random = new Random();

    private final List<String> fileNames = getAllTestPhotos();

    private List<String> getAllTestPhotos() {
        List<String> list = new ArrayList<>();
        Path rootPath = Paths.get("external/test-data/photos");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(rootPath)) {
            for (Path path : directoryStream) {
                list.add(path.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            log.error("Can't load photos path", e);
        }
        Collections.shuffle(list);
        return list;
    }

    public List<String> generatePhotos(int photoCount) {
        List<String> photos = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < photoCount; i++) {
            if (index >= fileNames.size()) {
                index = 0;
            }
            photos.add(fileNames.get(index++));
        }
        return Collections.unmodifiableList(photos);
    }
}
