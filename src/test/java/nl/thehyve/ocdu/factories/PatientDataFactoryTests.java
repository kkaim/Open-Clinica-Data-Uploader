package nl.thehyve.ocdu.factories;

import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.services.OpenClinicaService;
import nl.thehyve.ocdu.soap.ResponseHandlers.GetStudyMetadataResponseHandler;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by bo on 6/20/16.
 */
public class PatientDataFactoryTests {

    private PatientDataFactory factory;
    private UploadSession uploadSession;
    private OcUser user;
    private MetaData metadata;
    private Map<String, String> subjectMap;
    private Map<String, String> subjectSiteMap;


    @Autowired
    OpenClinicaService openClinicaService;


    @Before
    public void setUp() throws Exception {
        String username = "tester";
        this.user = new OcUser();
        this.user.setUsername(username);
        this.uploadSession = new UploadSession("testSubmission", UploadSession.Step.MAPPING, new Date(), this.user);
        this.factory = new PatientDataFactory(user, uploadSession, false);
        try {
            MessageFactory messageFactory = MessageFactory.newInstance();
            FileInputStream in = new FileInputStream(new File("docs/responseExamples/Sjogren_STUDY1.xml"));

            SOAPMessage mockedResponseGetMetadata = messageFactory.createMessage(null, in);
            this.metadata = GetStudyMetadataResponseHandler.parseGetStudyMetadataResponse(mockedResponseGetMetadata);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.subjectMap = new HashMap<>();
        this.subjectMap.put("test_ssid_1", null);
        this.subjectSiteMap = new HashMap<>();
        this.subjectSiteMap.put("test_ssid_1", "testSite");
    }

    @Test
    public void testCreatePatientsData() {
        Path testFilePath = Paths.get("docs/exampleFiles/subjects.tsv");

        List<Subject> subjects = factory.createPatientData(testFilePath);

        assertThat(subjects, contains(
                allOf(
                        hasProperty("owner", equalTo(user)),
                        hasProperty("submission", equalTo(uploadSession)),
                        hasProperty("ssid", equalTo("test_ssid_1")),
                        hasProperty("gender", equalTo("m")),
                        hasProperty("dateOfBirth", equalTo("01-JAN-1980")),
                        hasProperty("dateOfEnrollment", equalTo("01-FEB-2000")),
                        hasProperty("secondaryId", equalTo("1234567")),
                        hasProperty("study", equalTo("test_study_1")),
                        hasProperty("site", equalTo("test_site_1"))
                ),
                allOf(
                        hasProperty("owner", equalTo(user)),
                        hasProperty("submission", equalTo(uploadSession)),
                        hasProperty("ssid", equalTo("test_ssid_2")),
                        hasProperty("gender", equalTo("f")),
                        hasProperty("dateOfBirth", equalTo("01-JAN-1981")),
                        hasProperty("dateOfEnrollment", equalTo("01-FEB-2001")),
                        hasProperty("secondaryId", equalTo("12345678")),
                        hasProperty("study", equalTo("test_study_2")),
                        hasProperty("site", equalTo("test_site_2"))
                )
        ));
    }

    @Test
    public void testGeneratePatientRegistrationTemplate() {
        metadata.setBirthdateRequired(MetaData.BIRTH_DATE_AS_FULL_DATE);
        List<String> template = factory.generatePatientRegistrationTemplate(this.metadata, this.subjectMap, true, subjectSiteMap);
        assertEquals("Study Subject ID\tGender\tDate of Birth\tPerson ID\tDate of Enrollment\tSecondary ID\tUnique Protocol ID\tSite (optional)\n", template.get(0));
        assertEquals("test_ssid_1\t\t\t\t\t\tSjogren\ttestSite\n", template.get(1));

        metadata.setBirthdateRequired(MetaData.BIRTH_DATE_AS_ONLY_YEAR);
        template = factory.generatePatientRegistrationTemplate(this.metadata, this.subjectMap, true, subjectSiteMap);
        assertEquals("Study Subject ID\tGender\tYear of Birth\tPerson ID\tDate of Enrollment\tSecondary ID\tUnique Protocol ID\tSite (optional)\n", template.get(0));
        assertEquals("test_ssid_1\t\t\t\t\t\tSjogren\ttestSite\n", template.get(1));
    }


}
