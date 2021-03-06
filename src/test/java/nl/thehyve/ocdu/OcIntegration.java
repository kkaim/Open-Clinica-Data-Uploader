package nl.thehyve.ocdu;

import nl.thehyve.ocdu.models.OCEntities.Study;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.errors.AbstractMessage;
import nl.thehyve.ocdu.services.OpenClinicaService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

/**
 * Created by piotrzakrzewski on 29/04/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OcduApplication.class)
@WebAppConfiguration
public class OcIntegration {

    @Autowired
    OpenClinicaService openClinicaService;

    String sha1hexDigest = "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8";
    String ocUrl = "http://ocdu-openclinica-dev.thehyve.net/OpenClinica-ws"; // watch out for it, it is not guaranteed to be up. this is why this test suite is
    // excluded from Gradle test task.
    String user = "integration";

    @Ignore("Too expensive for frequent checks")
    @Test
    public void isAuthenticatedNegativeTest() throws Exception {
        boolean authenticated = openClinicaService.isAuthenticated("nonexistent_user", "withboguspassword", ocUrl);
        assertEquals(false, authenticated);
    }

    @Ignore("Too expensive for frequent checks")
    @Test
    public void isAuthenticatedPositiveTest() throws Exception {
        boolean authenticated = openClinicaService.isAuthenticated(user, sha1hexDigest, ocUrl);
        assertEquals(true, authenticated);
    }

    @Ignore("Too expensive for frequent checks")
    @Test
    public void listStudiesTest() throws Exception {
        // Watch out for this one. This will obviously fail if there are no studies loaded into OC, even if the call works
        List<Study> studies = openClinicaService.listStudies(user, sha1hexDigest, ocUrl);
        assertEquals(true, studies.size() > 0);
        assertThat(
                studies,
                everyItem(is(allOf(notNullValue(), instanceOf(Study.class)))));
    }

    @Ignore("Too expensive for frequent checks")
    @Test
    public void getMetaDataTest() throws Exception {
        String studyID = "Study 1";
        Study study = new Study(studyID, "", ""); // Only Identifier should be used for this call
        Set<String> sitesPresentInData = new HashSet<>();
        sitesPresentInData.add(studyID);
        MetaData metadata = openClinicaService.getMetadata(user, sha1hexDigest, ocUrl, study, sitesPresentInData);
        assertThat(metadata, is(notNullValue()));
    }

    @Ignore("Not idempotent operation - OC database needs to be rolled-back after")
    @Test
    public void registerPatients() throws Exception {
        Subject s1 = new Subject();

        s1.setSsid("createdByWebServ");
        s1.setStudy("Sjogren");
        s1.setDateOfBirth("1988");
        s1.setGender("m");
        Collection<Subject> subjects = new ArrayList<>();
        subjects.add(s1);
        AbstractMessage result = openClinicaService.registerPatient(user, sha1hexDigest, ocUrl, s1);
        assertFalse(result.isError()); // false if successful
    }
}
