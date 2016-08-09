package nl.thehyve.ocdu.controllers;

import nl.thehyve.ocdu.models.OCEntities.*;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.models.errors.AbstractMessage;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import nl.thehyve.ocdu.repositories.ClinicalDataRepository;
import nl.thehyve.ocdu.repositories.EventRepository;
import nl.thehyve.ocdu.repositories.SubjectRepository;
import nl.thehyve.ocdu.services.DataService;
import nl.thehyve.ocdu.services.OcUserService;
import nl.thehyve.ocdu.services.OpenClinicaService;
import nl.thehyve.ocdu.services.UploadSessionService;
import org.openclinica.ws.beans.StudySubjectWithEventsType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the upload of ODM-data to OpenClinica.
 * Created by Jacob Rousseau on 28-Jun-2016.
 * Copyright CTMM-TraIT / NKI (c) 2016
 */
@Controller
@RequestMapping("/odm")
public class ODMUploadController {

    @Autowired
    UploadSessionService uploadSessionService;

    @Autowired
    OcUserService ocUserService;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    OpenClinicaService openClinicaService;

    @Autowired
    DataService dataService;

    @Autowired
    ClinicalDataRepository clinicalDataRepository;

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity<Collection<AbstractMessage>> uploadODM(HttpSession session) {
        Collection<AbstractMessage> result = new ArrayList<>();
        try {
            UploadSession uploadSession = uploadSessionService.getCurrentUploadSession(session);

            OcUser user = ocUserService.getCurrentOcUser(session);
            String userName = user.getUsername();
            String pwdHash = ocUserService.getOcwsHash(session);
            String url = user.getOcEnvironment();

            Study study = dataService.findStudy(uploadSession.getStudy(), user, pwdHash);
            MetaData metaData =
                    openClinicaService.getMetadata(userName, pwdHash, user.getOcEnvironment(), study);

            List<StudySubjectWithEventsType> studySubjectWithEventsTypeList =
                    openClinicaService.getStudySubjectsType(userName, pwdHash, url, study.getIdentifier(), "");

            Collection<Subject> subjects = subjectRepository.findBySubmission(uploadSession);
            List<Event> eventList = eventRepository.findBySubmission(uploadSession);
            List<ClinicalData> clinicalDataList = clinicalDataRepository.findBySubmission(uploadSession);

            Collection<UploadDataUnit> uploadDataUnitList = createUploadDataUnitList(subjects, eventList, clinicalDataList, studySubjectWithEventsTypeList);


            for (UploadDataUnit uploadDataUnit : uploadDataUnitList) {
                if (! uploadDataUnit.isSubjectRegisteredInOpenClinica()) {
                    AbstractMessage resultMessage = openClinicaService.registerPatient(userName, pwdHash, url, uploadDataUnit.getSubject());
                    result.add(resultMessage);
                }
                List<Event> eventListPerSubject = uploadDataUnit.getEventList();

                if (! eventListPerSubject.isEmpty()) {
                    Collection<AbstractMessage> resultEventRegistration =
                            openClinicaService.scheduleEvents(userName, pwdHash, url, metaData, eventListPerSubject, studySubjectWithEventsTypeList);
                    result.addAll(resultEventRegistration);
                }

                String crfStatusAfterUpload = "initial data entry";
                List<ClinicalData> clinicalDataListPerSubject = uploadDataUnit.getClinicalDataList();
                if (! clinicalDataListPerSubject .isEmpty()) {
                    Collection<AbstractMessage> resultDataUpload =
                    openClinicaService.uploadODM(userName, pwdHash, url, clinicalDataListPerSubject, metaData, uploadSession, crfStatusAfterUpload);
                    result.addAll(resultDataUpload);
                }
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            ValidationErrorMessage errorMessage = new ValidationErrorMessage(e.getMessage());
            result.add(errorMessage);
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    private Collection<UploadDataUnit> createUploadDataUnitList(Collection<Subject> subjectList, List<Event> eventList, List<ClinicalData> clinicalDataList, List<StudySubjectWithEventsType> studySubjectWithEventsTypeList) {
        Map<String, UploadDataUnit> ret = new HashMap<>();
        for (Subject subject : subjectList) {
            String subjectID = subject.getSsid();
            UploadDataUnit uploadDataUnit = new UploadDataUnit(subject, false);
            ret.put(subject.getSsid(), uploadDataUnit);
        }

        for (Event event : eventList) {
            String subjectID = event.getSsid();
            List<StudySubjectWithEventsType> eventsForSubject = studySubjectWithEventsTypeList.stream()
                    .filter(studySubjectWithEventsType -> subjectID.equalsIgnoreCase(studySubjectWithEventsType.getLabel().toUpperCase()))
                    .collect(Collectors.toList());
            boolean subjectRegisteredInOpenClinica = ! eventsForSubject.isEmpty();
            UploadDataUnit uploadDataUnit = retrieveUploadDataUnit(ret, subjectID, subjectRegisteredInOpenClinica);
            uploadDataUnit.addEvent(event);
        }

        for (ClinicalData clinicalData : clinicalDataList) {
            String subjectID = clinicalData.getSsid();

            List<StudySubjectWithEventsType> eventsForSubject = studySubjectWithEventsTypeList.stream()
                    .filter(studySubjectWithEventsType -> subjectID.equalsIgnoreCase(studySubjectWithEventsType.getLabel().toUpperCase()))
                    .collect(Collectors.toList());
            boolean subjectRegisteredInOpenClinica = ! eventsForSubject.isEmpty();

            UploadDataUnit uploadDataUnit = retrieveUploadDataUnit(ret, subjectID, subjectRegisteredInOpenClinica);
            uploadDataUnit.addClinicalData(clinicalData);
        }
        return ret.values();
    }

    /**
     * Retrieves / creates a {@link UploadDataUnit} for the upload-run.
     * @param uploadDataUnitMap the map of the subjectID and the {@link UploadDataUnit}
     * @param subjectID the subject ID of the subject we are looking for
     * @return {@link UploadDataUnit} in the map.
     */
    private UploadDataUnit retrieveUploadDataUnit(Map<String, UploadDataUnit> uploadDataUnitMap, String subjectID, boolean subjectRegisteredInOpenClinica) {
        UploadDataUnit uploadDataUnit = uploadDataUnitMap.get(subjectID);
        if (uploadDataUnit == null) {
            Subject newSubject = new Subject();
            newSubject.setSsid(subjectID);
            uploadDataUnit = new UploadDataUnit(newSubject, subjectRegisteredInOpenClinica);
            uploadDataUnitMap.put(subjectID, uploadDataUnit);
        }
        return uploadDataUnit;
    }
}
