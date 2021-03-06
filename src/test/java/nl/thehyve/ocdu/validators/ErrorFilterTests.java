package nl.thehyve.ocdu.validators;

import nl.thehyve.ocdu.TestUtils;
import nl.thehyve.ocdu.models.OCEntities.ClinicalData;
import nl.thehyve.ocdu.models.OCEntities.Event;
import nl.thehyve.ocdu.models.OCEntities.Study;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.StringListNotificationsCollector;
import nl.thehyve.ocdu.models.errors.AbstractMessage;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.soap.ResponseHandlers.GetStudyMetadataResponseHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the class {@link ErrorFilter}.
 * Created by jacob on 11/11/16.
 */
public class ErrorFilterTests {

    private static final String STUDY_NAME = "EVENTFUL";
    private static final String STUDY_OID = "S_EVENTFUL";

    private List<Subject> subjectList;
    private List<Event> eventList;
    private List<ClinicalData> clinicalDataList;
    private StringListNotificationsCollector notificationsCollector;

    private ErrorFilter errorFilter;


    @Before
    public void setUp() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        File testFile = new File("docs/responseExamples/getStudyMetadata3.xml");
        FileInputStream in = new FileInputStream(testFile);
        SOAPMessage mockedResponseGetMetadata = messageFactory.createMessage(null, in);//soapMessage;
        MetaData metaData = GetStudyMetadataResponseHandler.parseGetStudyMetadataResponse(mockedResponseGetMetadata);

        List<StudySubjectWithEventsType> subjectWithEventsTypeList = TestUtils.createStudySubjectWithEventList();
        Study study = new Study(STUDY_NAME, STUDY_OID, STUDY_NAME);
        subjectList = new ArrayList<>();
        eventList = new ArrayList<>();
        clinicalDataList = new ArrayList<>();
        notificationsCollector = new StringListNotificationsCollector("http://www.example.com");
        errorFilter =
                new ErrorFilter(study, metaData, subjectWithEventsTypeList, clinicalDataList, eventList, subjectList, notificationsCollector);
    }


    @Test
    public void testBlockEntireUploadForSubject() {
        Subject subject = new Subject();
        subject.setSsid("Piet Pietersen");
        subject.addErrorClassification(ErrorClassification.BLOCK_ENTIRE_UPLOAD);
        subjectList.add(subject);
        errorFilter.filterDataWithErrors();

        Assert.assertTrue(subjectList.isEmpty());
        AbstractMessage  notification = notificationsCollector.getNotificationList().get(0);
        assertThat(notification.getMessage(), containsString("An error is present which blocks the entire upload"));
    }

    @Test
    public void testBlockEntireUploadForEvent() {
        Event event = new Event();
        event.setSsid("Piet Pietersen");
        event.setStudy(STUDY_NAME);
        event.addErrorClassification(ErrorClassification.BLOCK_ENTIRE_UPLOAD);
        eventList.add(event);
        errorFilter.filterDataWithErrors();

        Assert.assertTrue(eventList.isEmpty());
        AbstractMessage notification = notificationsCollector.getNotificationList().get(0);
        assertThat(notification.getMessage(), containsString("An error is present which blocks the entire upload"));
    }

    @Test
    public void testBlockEntireUploadForClinicalData() {
        ClinicalData clinicalData = new ClinicalData();
        clinicalData.setSsid("Piet Pietersen");
        clinicalData.addErrorClassification(ErrorClassification.BLOCK_ENTIRE_UPLOAD);
        clinicalDataList.add(clinicalData);
        errorFilter.filterDataWithErrors();

        Assert.assertTrue(eventList.isEmpty());
        AbstractMessage notification = notificationsCollector.getNotificationList().get(0);
        assertThat(notification.getMessage(), containsString("An error is present which blocks the entire upload"));
    }

    @Test
    public void testEventsWithError() {
        // Test scenario: 2 subjects, with 1 subject having a repeating event, one with an error event and one which is
        // OK. Second subject (Miss Piggy) has no errors on event level but only one error on item-level.
        // // Because we are dealing with an repeating eventExpected
        // the result should be that all the events of 'Kermit the frog' are removed.
        String kermit = "EV-00002";
        String missPiggy = "EV-00006";
        String theEventName = "REPEATINGEVENT";
        Subject subjectKermit = new Subject();
        subjectKermit.setStudy(STUDY_NAME);
        subjectKermit.setSsid(kermit);
        subjectList.add(subjectKermit);

        Subject subjectMissPiggy = new Subject();
        subjectMissPiggy.setStudy(STUDY_NAME);
        subjectMissPiggy.setSsid(missPiggy);
        subjectList.add(subjectMissPiggy);

        Event eventToRemove = new Event();
        eventToRemove.setStudy(STUDY_NAME);
        eventToRemove.setSsid(kermit);
        eventToRemove.setEventName(theEventName);
        eventToRemove.setRepeatNumber("1");
        eventToRemove.addErrorClassification(ErrorClassification.BLOCK_EVENT);
        eventList.add(eventToRemove);

        Event eventWithOutSubject = new Event();
        eventWithOutSubject.setStudy(STUDY_NAME);
        eventWithOutSubject.setSsid("");
        eventWithOutSubject.setEventName(theEventName);
        eventWithOutSubject.setRepeatNumber("1");
        eventWithOutSubject.addErrorClassification(ErrorClassification.BLOCK_EVENT);
        eventList.add(eventWithOutSubject);

        Event eventWithOutEventName = new Event();
        eventWithOutEventName.setStudy(STUDY_NAME);
        eventWithOutEventName.setSsid(kermit);
        eventWithOutEventName.setEventName("");
        eventWithOutEventName.setRepeatNumber("3");
        eventWithOutEventName.addErrorClassification(ErrorClassification.BLOCK_EVENT);
        eventList.add(eventWithOutEventName);

        Event eventWithOutStudyName = new Event();
        eventWithOutStudyName.setStudy("");
        eventWithOutStudyName.setSsid(kermit);
        eventWithOutStudyName.setEventName(theEventName);
        eventWithOutStudyName.setRepeatNumber("4");
        eventWithOutStudyName.addErrorClassification(ErrorClassification.BLOCK_EVENT);
        eventList.add(eventWithOutStudyName);

        Event eventWithOutRepeatNumber = new Event();
        eventWithOutRepeatNumber.setStudy(STUDY_NAME);
        eventWithOutRepeatNumber.setSsid(kermit);
        eventWithOutRepeatNumber.setEventName(theEventName);
        eventWithOutRepeatNumber.setRepeatNumber("");
        eventWithOutRepeatNumber.addErrorClassification(ErrorClassification.BLOCK_EVENT);
        eventList.add(eventWithOutRepeatNumber);

        Event event = new Event();
        event.setStudy(STUDY_NAME + "asdf");
        event.setSsid(kermit);
        event.setEventName(theEventName);
        event.setRepeatNumber("2");
        eventList.add(event);

        Event eventToRemain = new Event();
        eventToRemain.setStudy(STUDY_NAME);
        eventToRemain.setSsid(missPiggy);
        eventToRemain.setEventName(theEventName);
        eventToRemain.setRepeatNumber("1");
        eventList.add(eventToRemain);

        ClinicalData clinicalData = new ClinicalData();
        clinicalData.setStudy(STUDY_NAME);
        clinicalData.setSsid(kermit);
        clinicalData.setEventName(theEventName);
        clinicalData.setEventRepeat("2");
        clinicalDataList.add(clinicalData);

        clinicalData = new ClinicalData();
        clinicalData.setStudy(STUDY_NAME);
        clinicalData.setSsid(kermit);
        clinicalData.setEventName(theEventName);
        clinicalData.setEventRepeat("1");
        clinicalDataList.add(clinicalData);

        clinicalData = new ClinicalData();
        clinicalData.setStudy(STUDY_NAME);
        clinicalData.setSsid(missPiggy);
        clinicalData.setEventName(theEventName);
        clinicalData.setItem("This item should remain");
        clinicalData.setEventRepeat("1");
        clinicalDataList.add(clinicalData);

        clinicalData = new ClinicalData();
        clinicalData.setStudy(STUDY_NAME);
        clinicalData.setSsid(missPiggy);
        clinicalData.setEventName(theEventName);
        clinicalData.setEventRepeat("1");
        clinicalData.addErrorClassification(ErrorClassification.BLOCK_SINGLE_ITEM);
        clinicalDataList.add(clinicalData);

        errorFilter.filterDataWithErrors();
        Assert.assertEquals(2, subjectList.size());
        Assert.assertTrue(subjectList.contains(subjectKermit));
        Assert.assertTrue(subjectList.contains(subjectMissPiggy));
        Assert.assertTrue(eventList.contains(eventToRemain));
        Assert.assertEquals(1, clinicalDataList.size());
    }

    @Test
    public void testBlockCRFWithError() {
        // Test scenario: 2 subjects, 3 clinical data entry with one having a Block_entire_CRF error.
        String kermit = "EV-00002";
        String missPiggy = "EV-00006";
        String theEventName = "REPEATINGEVENT";
        Subject subjectKermit = new Subject();
        subjectKermit.setStudy(STUDY_NAME);
        subjectKermit.setSsid(kermit);
        subjectList.add(subjectKermit);

        Subject subjectMissPiggy = new Subject();
        subjectMissPiggy.setStudy(STUDY_NAME);
        subjectMissPiggy.setSsid(missPiggy);
        subjectList.add(subjectMissPiggy);


        Event event = new Event();
        event.setStudy(STUDY_NAME);
        event.setSsid(kermit);
        event.setEventName(theEventName);
        event.setRepeatNumber("1");
        eventList.add(event);


        ClinicalData clinicalData = new ClinicalData();
        clinicalData.setStudy(STUDY_NAME);
        clinicalData.setSsid(kermit);
        clinicalData.setEventName(theEventName);
        clinicalData.setEventRepeat("1");
        clinicalData.setValue("Aap");
        clinicalDataList.add(clinicalData);

        clinicalData = new ClinicalData();
        clinicalData.setStudy(STUDY_NAME);
        clinicalData.setSsid(missPiggy);
        clinicalData.setEventName(theEventName);
        clinicalData.setEventRepeat("1");
        clinicalData.setValue("Noot");
        clinicalDataList.add(clinicalData);


        clinicalData = new ClinicalData();
        clinicalData.setStudy(STUDY_NAME);
        clinicalData.setSsid(kermit);
        clinicalData.setEventName(theEventName);
        clinicalData.setEventRepeat("2");
        clinicalData.setValue("Mies");
        clinicalData.addErrorClassification(ErrorClassification.BLOCK_ENTIRE_CRF);
        clinicalDataList.add(clinicalData);

        errorFilter.filterDataWithErrors();
        List<String> valueList =
                clinicalDataList.stream().map(ClinicalData::getValue).collect(Collectors.toList());
        Assert.assertEquals(2, subjectList.size());
        Assert.assertTrue(subjectList.contains(subjectKermit));
        Assert.assertTrue(subjectList.contains(subjectMissPiggy));
        Assert.assertFalse(valueList.contains("Mies"));
        Assert.assertEquals(2, clinicalDataList.size());
    }




    @Test
    public void testClinicalDataWithError() {
        // Test scenario: 2 subjects, with 1 subject having a repeating event, one with an error event and one which is
        // OK. Second subject (Miss Piggy) has no errors. Because we are dealing with an repeating eventExpected
        // the result should be that all the events of 'Kermit the frog' are removed.
        String testSubjectID = "Kermit the frog";
        String testSubjectID_Two = "Miss Piggy";
        String theEventName = "RepeatingEvent";
        Subject subjectKermit = new Subject();
        subjectKermit.setStudy(STUDY_NAME);
        subjectKermit.setSsid(testSubjectID);
        subjectList.add(subjectKermit);

        Subject subjectMissPiggy = new Subject();
        subjectMissPiggy.setStudy(STUDY_NAME);
        subjectMissPiggy.setSsid(testSubjectID_Two);
        subjectList.add(subjectMissPiggy);


        Event event1 = new Event();
        event1.setStudy(STUDY_NAME);
        event1.setSsid(testSubjectID);
        event1.setEventName(theEventName);
        event1.setRepeatNumber("2");
        eventList.add(event1);

        Event event2 = new Event();
        event2.setStudy(STUDY_NAME);
        event2.setSsid(testSubjectID_Two);
        event2.setEventName(theEventName);
        event2.setRepeatNumber("1");
        eventList.add(event2);

        ClinicalData clinicalData = new ClinicalData();
        clinicalData.setStudy(STUDY_NAME);
        clinicalData.setSsid(testSubjectID);
        clinicalData.setEventName(theEventName);
        clinicalData.setEventRepeat("1");
        clinicalData.setItem("Item1");
        clinicalData.setValue("ABCDEDFG");
        clinicalDataList.add(clinicalData);

        clinicalData = new ClinicalData();
        clinicalData.setStudy(STUDY_NAME);
        clinicalData.setSsid(testSubjectID);
        clinicalData.setEventName(theEventName);
        clinicalData.setItem("Item2");
        clinicalData.setValue("1234567");
        clinicalData.addErrorClassification(ErrorClassification.BLOCK_SINGLE_ITEM);
        clinicalData.setEventRepeat("1");
        clinicalDataList.add(clinicalData);

        errorFilter.filterDataWithErrors();
        Assert.assertEquals(2, subjectList.size());
        Assert.assertTrue(subjectList.contains(subjectKermit));
        Assert.assertTrue(subjectList.contains(subjectMissPiggy));
        Assert.assertTrue(eventList.contains(event1));
        Assert.assertTrue(eventList.contains(event2));
        Assert.assertEquals(1, clinicalDataList.size());
        ClinicalData remainingData = clinicalDataList.get(0);
        Assert.assertEquals("ABCDEDFG", remainingData.getValue());
    }

    @Test
    public void testBlockSubjectsWithError() {
        String theEventName = "REPEATINGEVENT";
        String testSubjectID = "Thomas the Tank-engine";
        Subject subject = new Subject();
        subject.setSsid(testSubjectID);
        subject.setStudy(STUDY_NAME);
        subject.addErrorClassification(ErrorClassification.BLOCK_SUBJECT);
        subjectList.add(subject);

        subject = new Subject();
        subject.setSsid("Henry");
        subject.setStudy(STUDY_NAME);
        subjectList.add(subject);

        subject = new Subject();
        subject.setSsid("James");
        subject.setStudy(STUDY_NAME);
        subject.addErrorClassification(ErrorClassification.BLOCK_SUBJECT);
        subjectList.add(subject);

        Event event = new Event();
        event.setSsid(testSubjectID);
        event.setEventName(theEventName);
        event.setStudy(STUDY_NAME);
        eventList.add(event);

        event = new Event();
        event.setSsid("Henry");
        event.setEventName(theEventName);
        event.setStudy(STUDY_NAME);
        eventList.add(event);

        ClinicalData clinicalData = new ClinicalData();
        clinicalData.setSsid(testSubjectID);
        clinicalData.setItem("Favorite food");
        clinicalData.setValue("Yorkshire black coal");
        clinicalDataList.add(clinicalData);


        errorFilter.filterDataWithErrors();

        Assert.assertEquals(1, subjectList.size());
        Subject remainingSubject = subjectList.get(0);
        Assert.assertEquals("Henry", remainingSubject.getSsid());

        Assert.assertEquals(1, eventList.size());
        Event remainingEvent = eventList.get(0);
        Assert.assertEquals("Henry", remainingEvent.getSsid());

        Assert.assertTrue(clinicalDataList.isEmpty());
    }
}
