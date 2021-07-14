package com.demidrolll.myphotos.generator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

public class DownloadPhotosFromUnsplash {

    private static final Logger log = LoggerFactory.getLogger(DownloadPhotosFromUnsplash.class);

    public static void main(String[] args) throws Exception {
        try {
            new DownloadPhotosFromUnsplash().execute(
                    "https://api.unsplash.com/photos?per_page=30",
                    Path.of("external/test-data/photos")
            );
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    public void execute(String unsplashSourceUrl, Path destinationDirectoryPath) throws IOException {
        createDestinationIfNecessary(destinationDirectoryPath);
        Response response = getPhotoLinks(unsplashSourceUrl);
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            parseValidResponse(response, destinationDirectoryPath);
        } else {
            displayErrorMessage(response);
        }
    }

    private void displayErrorMessage(Response response) {
        log.error("Request unsplash failed: {}", response);
    }

    private void parseValidResponse(Response response, Path destinationDirectoryPath) {
        log.info("X-Ratelimit-Remaining={}", response.getHeaderString("X-Ratelimit-Remaining"));

        List<Item> items = response.readEntity(new GenericType<List<Item>>() {});
        int id = 1;
        for (Item item : items) {
            Path file = Paths.get(String.format("%s/%s.jpg", destinationDirectoryPath.toAbsolutePath(), id));
            downloadImage(item.getLinks().getDownload(), file);
            id++;
            log.info("Successful downloaded {}", item.getLinks().getDownload());
        }
    }

    private void downloadImage(String url, Path path) {
        Response response = ClientBuilder
                .newClient()
                .register(new FollowRedirectFilter())
                .target(url)
                .request("image/jpeg")
                .get();

        try (InputStream in = response.readEntity(InputStream.class)) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Download file failed", e);
        }
    }

    private Response getPhotoLinks(String unsplashSourceUrl) {
        Client client = ClientBuilder.newClient();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        client.register(new JacksonJaxbJsonProvider(objectMapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS));

        return client
                .target(unsplashSourceUrl)
                .request(MediaType.APPLICATION_JSON)
                .header("Accept-Version", "v1")
                .header("Authorization", "Client-ID " + getSystemEnvironmentVariable("UNSPLASH_KEY"))
                .get();

    }

    private String getSystemEnvironmentVariable(String name) {
        return Optional
                .ofNullable(System.getenv().get(name))
                .orElseThrow(() -> new IllegalStateException(String.format("System variable not defined: %s", name)));
    }

    private void createDestinationIfNecessary(Path destiantionDirectoryPath) throws IOException {
        if (Files.notExists(destiantionDirectoryPath)) {
            Files.createDirectories(destiantionDirectoryPath);
        }
    }

    private static class FollowRedirectFilter implements ClientResponseFilter {

        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
            if (responseContext.getStatusInfo().getFamily() == Response.Status.Family.REDIRECTION) {
                Response response = requestContext.getClient()
                        .target(responseContext.getLocation())
                        .request()
                        .method(requestContext.getMethod());

                responseContext.setEntityStream((InputStream) response.getEntity());
                responseContext.setStatusInfo(response.getStatusInfo());
                responseContext.setStatus(response.getStatus());
            }
        }
    }

    private static class Item {

        private Links links;

        public Links getLinks() {
            return this.links;
        }

        public void setLinks(Links links) {
            this.links = links;
        }
    }

    private static class Links {

        private String download;

        public String getDownload() {
            return this.download;
        }

        public void setDownload(String download) {
            this.download = download;
        }
    }
}
