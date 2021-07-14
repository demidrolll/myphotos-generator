package com.demidrolll.myphotos.generator.component;

import com.demidrolll.myphotos.exception.ApplicationException;
import com.demidrolll.myphotos.model.domain.Profile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class ProfileGenerator {

    private final Random random = new Random();

    public List<Profile> generateProfiles() {
        File file = new File("external/test-data/profiles.xml");
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Profiles.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Profiles profiles = (Profiles) jaxbUnmarshaller.unmarshal(file);
            final Date created = new Date();
            return profiles.getProfile().stream()
                    .peek(profile -> {
                        profile.setUid(String.format("%s-%s", profile.getFirstName(), profile.getLastName()).toLowerCase());
                        profile.setEmail(profile.getUid() + "@myphotos.com");
                        profile.setPhotoCount(random.nextInt(15) + random.nextInt(5) + 3);
                        profile.setCreated(created);
                    })
                    .collect(Collectors.toUnmodifiableList());
        } catch (JAXBException e) {
            log.error("Can't load test data from {}", file.getAbsolutePath());
            throw new ApplicationException("Can't load test data", e);
        }

    }

    @XmlRootElement(name = "profiles")
    @Getter
    @Setter
    private static class Profiles {

        private List<Profile> profile;
    }
}
