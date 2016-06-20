package nl.thehyve.ocdu.soap;

import nl.thehyve.ocdu.models.OCEntities.Study;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.SiteDefinition;
import org.junit.Test;
import org.openclinica.ws.beans.GenderType;
import org.openclinica.ws.beans.StudySubjectType;
import org.openclinica.ws.beans.SubjectType;
import org.openclinica.ws.studysubject.v1.CreateRequest;

import javax.xml.datatype.XMLGregorianCalendar;

import java.util.ArrayDeque;
import java.util.Collection;

import static nl.thehyve.ocdu.soap.SOAPRequestFactories.CreateSubjectRequestFactory.getCreateRequests;
import static nl.thehyve.ocdu.soap.SOAPRequestFactories.StudySubjectFactory.createStudySubject;
import static nl.thehyve.ocdu.soap.SOAPRequestFactories.StudySubjectFactory.createSubjectType;
import static nl.thehyve.ocdu.soap.SOAPRequestFactories.StudySubjectFactory.createXMLGregorianDate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by piotrzakrzewski on 17/06/16.
 */

public class SOAPRequestFactoriesTests {

    @Test
    public void createSubjectRequestTest() throws Exception {
        Subject s1 = new Subject();
        s1.setSsid("s1");
        Subject s2 = new Subject();
        s2.setSsid("s2");
        Collection<Subject> subjects = new ArrayDeque<>();
        subjects.add(s1);
        subjects.add(s2);
        Study study = new Study("study1","oid","study");
        SiteDefinition site = null;
        Collection<CreateRequest> createRequests = getCreateRequests(subjects, study, site);
        assertThat(createRequests, iterableWithSize(2));
    }

    @Test
    public void createStudySubjecttest() throws Exception {
        SiteDefinition site = null;
        Study study = new Study("study1", "oid", "study");
        Subject subject = new Subject();
        subject.setSsid("subject1");

        StudySubjectType studySubject = createStudySubject(subject, study, site);
        assertThat(studySubject, is(notNullValue()));
    }

    @Test
    public void createXMLGregorianDateTest() throws Exception {
        Subject subject = new Subject();
        subject.setDateOfBirth("01-Jan-1996");
        XMLGregorianCalendar xmlGregorianDate = createXMLGregorianDate(subject.getDateOfBirth());
        int year = xmlGregorianDate.getYear();
        assertThat(year, equalTo(1996));
    }

    @Test
    public void createSubjectTypeTests_noGender_noDate() throws Exception {
        Subject subject = new Subject();
        SubjectType subjectType = createSubjectType(subject);
        assertThat(subjectType, notNullValue());
        assertThat(subjectType, hasProperty("gender", nullValue()));
        assertThat(subjectType, hasProperty("dateOfBirth", nullValue()));
    }

    @Test
    public void createSubjectTypeTests_noDate() throws Exception {
        Subject subject = new Subject();
        subject.setGender("f");
        SubjectType subjectType = createSubjectType(subject);
        assertThat(subjectType, notNullValue());
        assertThat(subjectType, hasProperty("gender", is(GenderType.F)));
        assertThat(subjectType, hasProperty("dateOfBirth", nullValue()));
    }

    @Test
    public void createSubjectTypeTests_noGender() throws Exception {
        Subject subject = new Subject();
        subject.setDateOfBirth("2000");
        SubjectType subjectType = createSubjectType(subject);
        assertThat(subjectType, notNullValue());
        assertThat(subjectType, hasProperty("gender", nullValue()));
        assertThat(subjectType, hasProperty("dateOfBirth", notNullValue(XMLGregorianCalendar.class)));
    }

    @Test
    public void createSubjectTypeTests_allSet() throws Exception {
        Subject subject = new Subject();
        subject.setGender("f");
        subject.setDateOfBirth("2000");
        SubjectType subjectType = createSubjectType(subject);
        assertThat(subjectType, notNullValue());
        assertThat(subjectType, hasProperty("gender", is(GenderType.F)));
        assertThat(subjectType, hasProperty("dateOfBirth", notNullValue(XMLGregorianCalendar.class)));
    }


}
