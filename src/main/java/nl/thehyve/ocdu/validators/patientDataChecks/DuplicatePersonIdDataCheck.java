package nl.thehyve.ocdu.validators.patientDataChecks;

import nl.thehyve.ocdu.models.OCEntities.Study;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.MetaData;
import nl.thehyve.ocdu.models.OcDefinitions.ProtocolFieldRequirementSetting;
import nl.thehyve.ocdu.models.errors.ValidationErrorMessage;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.StudySubjectWithEventsType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks if a person ID is allready been used in a study, only for study's who have set this field to required.
 * Created by jacob on 8/8/16.
 */
public class DuplicatePersonIdDataCheck implements PatientDataCheck {

    @Override
    public ValidationErrorMessage getCorrespondingError(int index, Subject subject, MetaData metaData,
                                                        List<StudySubjectWithEventsType> subjectWithEventsTypes,
                                                        Set<String> ssidsInData) {

        String ssid = subject.getSsid();
        String commonMessage = getCommonErrorMessage(index, ssid);

        ValidationErrorMessage error = null;
        String personId = subject.getPersonId();

        if ((metaData.getPersonIDUsage() == ProtocolFieldRequirementSetting.MANDATORY) & (StringUtils.isNotEmpty(personId) & (StringUtils.isNotEmpty(ssid)))) {
            List<StudySubjectWithEventsType> subjectPresentWithPersonIDList =
                    subjectWithEventsTypes.stream()
                            .filter(studySubjectWithEventsType ->
                                personId.equals(studySubjectWithEventsType.getSubject().getUniqueIdentifier()) &&
                                        ! ssid.equals(studySubjectWithEventsType.getLabel())
                            )
                            .collect(Collectors.toList());
            if (! subjectPresentWithPersonIDList.isEmpty()) {
                StudySubjectWithEventsType subjectWithEventsType = subjectPresentWithPersonIDList.get(0);
                error = new ValidationErrorMessage(commonMessage + "Duplicate Person ID in data. Is already present for subject with label " + subjectWithEventsType.getLabel());
            }
        }

        if(error != null) {
            error.addOffendingValue("Person ID: " + subject.getPersonId() + " for subject label " + ssid);
        }

        return error;
    }

}