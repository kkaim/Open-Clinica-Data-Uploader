/*
 * Copyright © 2016-2017 The Hyve B.V. and Netherlands Cancer Institute (NKI).
 *
 * This file is part of OCDI (OpenClinica Data Importer).
 *
 * OCDI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OCDI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OCDI. If not, see <http://www.gnu.org/licenses/>.
 */

package nl.thehyve.ocdu.models.OCEntities;

import nl.thehyve.ocdu.models.OcUser;
import nl.thehyve.ocdu.models.UploadSession;
import nl.thehyve.ocdu.models.errors.ErrorClassification;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.*;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Represents user-submitted ClinicalData point. Meant to store data as-is.
 * Created by piotrzakrzewski on 16/04/16.
 */
@Entity
public class ClinicalData implements OcEntity, UserSubmitted, EventReference {

    public static final String KEY_SEPARATOR = "\t";

    // The prefix and postfix to display the clinical data axis values.
    public static final String CD_SEP_PREFIX = "<i>";
    public static final String CD_SEP_POSTEFIX = "</i>, ";


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    private UploadSession submission;

    @ManyToOne
    private OcUser owner;
    @Column(columnDefinition = "TEXT")
    private String study;
    @Column(columnDefinition = "TEXT")
    private String site;
    @Column(columnDefinition = "TEXT")
    private String item;

    private String originalItem;

    private String ssid;

    private String personID;

    @Column(columnDefinition = "TEXT")
    private String eventName;
    private String eventRepeat;
    @Column(columnDefinition = "TEXT")
    private String crfName;

    @Column(columnDefinition = "TEXT")
    private String crfVersion;

    @Column(columnDefinition = "TEXT")
    private String itemGroupOID;

    private Integer groupRepeat;

    @Column(columnDefinition = "TEXT")
    private String value;
    private String studyProtocolName;

    private long lineNumber;

    @ElementCollection(targetClass=ErrorClassification.class)
    @Enumerated(EnumType.ORDINAL)
    @CollectionTable(name="clinical_data_errors", joinColumns = {@JoinColumn(name="id")})
    private Set<ErrorClassification> errorClassificationSet;


    public ClinicalData(long lineNumber, String study, String item, String ssid, String personID, String eventName, String eventRepeat, String crfName, UploadSession submission, String crfVersion, Integer groupRepeat, OcUser owner, String value) {
        this.study = study;
        this.item = item;
        this.ssid = ssid;
        this.personID = personID;
        this.eventName = eventName;
        this.eventRepeat = eventRepeat;
        this.crfName = crfName;
        this.submission = submission;
        this.crfVersion = crfVersion;
        this.itemGroupOID = "";
        this.groupRepeat = groupRepeat;
        this.owner = owner;
        this.value = value;
        this.originalItem = item; // TODO: Refactor away this constructor
        this.lineNumber = lineNumber;
        this.errorClassificationSet = new HashSet<>();
    }

    public ClinicalData() {
        this.errorClassificationSet = new HashSet<>();
    }

    public boolean hasErrorOfType(ErrorClassification errorClassification) {
        return errorClassificationSet.contains(errorClassification);
    }

    public void addErrorClassification(ErrorClassification errorClassification) {
        errorClassificationSet.add(errorClassification);
    }


    public String getPersonID() {
        return personID;
    }

    public void setPersonID(String personID) {
        this.personID = personID;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setSubmission(UploadSession submission) {
        this.submission = submission;
    }

    @Override
    public void setOwner(OcUser owner) {
        this.owner = owner;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    @Override
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEventRepeat(String eventRepeat) {
        this.eventRepeat = eventRepeat;
    }

    public void setCrfName(String crfName) {
        this.crfName = crfName;
    }

    public void setCrfVersion(String crfVersion) {
        this.crfVersion = crfVersion;
    }

    public void setGroupRepeat(Integer groupRepeat) {
        this.groupRepeat = groupRepeat;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public OcUser getOwner() {
        return owner;
    }

    public long getId() {
        return id;
    }

    @Override
    public UploadSession getSubmission() {
        return submission;
    }

    @Override
    public String getStudy() {
        return study;
    }

    public String getItem() {
        return item;
    }

    @Override
    public String getSsid() {
        return ssid;
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    public String getEventRepeat() {
        return eventRepeat;
    }

    public String getCrfName() {
        return crfName;
    }

    public String getCrfVersion() {
        return crfVersion;
    }

    public String getItemGroupOID() {
        return itemGroupOID;
    }

    public void setItemGroupOID(String itemGroupOID) {
        this.itemGroupOID = itemGroupOID;
    }

    public Integer getGroupRepeat() {
        return groupRepeat;
    }

    @Override
    public String getStudyProtocolName() {
        return studyProtocolName;
    }

    public void setStudyProtocolName(String studyProtocolName) {
        this.studyProtocolName = studyProtocolName;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return "ClinicalData{" +
                "error= '" + Arrays.toString(errorClassificationSet.toArray()) + '\'' +
                ", study='" + study + '\'' +
                ", site='" + site + '\'' +
                ", item='" + item + '\'' +
                ", ssid='" + ssid + '\'' +
                ", personID='" + personID + '\'' +
                ", eventName='" + eventName + '\'' +
                ", eventRepeat=" + eventRepeat +
                ", crfName='" + crfName + '\'' +
                ", crfVersion='" + crfVersion + '\'' +
                ", itemGroupOID='" + itemGroupOID + '\'' +
                ", groupRepeat=" + groupRepeat +
                ", value='" + value + '\'' +
                '}';
    }

    /**
     * Tests if a OCItemMapping has a conflicting CRF version. This is required to avoid uploads with similar study,
     * subject, event, event-ordinal and CRF-name but with a different CRF-versions. In such a situation OpenClinica web-services
     * adds 2 CRF's to the event. (TODO add the reference to the OC-bug)
     *
     * @param that
     * @return <code>true</code> if a conflicting CRF is present.
     */
    public boolean hasSameCRFVersion(ClinicalData that) {
        if ((this.study == null) ||
                (this.ssid == null) ||
                (this.eventName == null) ||
                (this.eventRepeat == null) ||
                (this.crfName == null) ||
                (this.crfVersion == null)) {
            throw new IllegalStateException("Unable to determine conflict state; a field is null");
        }
        if (!(this.study.equals(that.study) &&
                (this.ssid.equals(that.ssid)) &&
                (this.eventName.equals(that.eventName)) &&
                (this.eventRepeat.equals(that.eventRepeat)) &&
                (this.crfName.equals(that.crfName)))) {
            return false;
        }
        return this.crfVersion.equals(that.crfVersion);
    }

    /**
     * Tests if a OCItemMapping has a conflicting CRF version. This is required to avoid uploads with similar study,
     * subject, event, event-ordinal and CRF-name but with a different CRF-versions. In such a situation OpenClinica web-services
     * adds 2 CRF's to the event. (TODO add the reference to the OC-bug)
     *
     * @param that
     * @return <code>true</code> if a conflicting CRF is present.
     */
    public boolean isSameCRF(ClinicalData that) {
        if ((this.study == null) ||
                (this.ssid == null) ||
                (this.eventName == null) ||
                (this.eventRepeat == null) ||
                (this.crfName == null)) {
            throw new IllegalStateException("Unable to determine conflict state; a field is null");
        }
        return (this.study.equals(that.study) &&
                (this.ssid.equals(that.ssid)) &&
                (this.eventName.equals(that.eventName)) &&
                (this.eventRepeat.equals(that.eventRepeat)) &&
                (this.crfName.equals(that.crfName)));
    }

    /**
     * creates a key value for the ODM generation for each unique chunk of output.
     *
     * @return
     */
    public String createODMKey() {
        StringBuffer ret = new StringBuffer();
        ret.append(UtilChecks.nullSafeToUpperCase(ssid));
        ret.append(UtilChecks.nullSafeToUpperCase(eventName));
        ret.append(UtilChecks.nullSafeToUpperCase(eventRepeat));
        ret.append(UtilChecks.nullSafeToUpperCase(crfName));
        ret.append(UtilChecks.nullSafeToUpperCase(crfVersion));
        ret.append(UtilChecks.nullSafeToUpperCase(itemGroupOID));
//        ret.append(groupRepeat.toString());
        return ret.toString().toUpperCase();
    }

    public String createEventKey() {
        StringBuffer ret = new StringBuffer();
        ret.append(UtilChecks.nullSafeToUpperCase(study));
        ret.append(KEY_SEPARATOR);
        ret.append(UtilChecks.nullSafeToUpperCase(site));
        ret.append(KEY_SEPARATOR);
        ret.append(UtilChecks.nullSafeToUpperCase(ssid));
        ret.append(KEY_SEPARATOR);
        ret.append(UtilChecks.nullSafeToUpperCase(eventName));
        ret.append(KEY_SEPARATOR);
        ret.append(UtilChecks.nullSafeToUpperCase(eventRepeat));
        return ret.toString().toUpperCase();
    }

    public String createEventKeyWithoutRepeat() {
        StringBuffer ret = new StringBuffer();
        ret.append(study);
        ret.append(KEY_SEPARATOR);
        ret.append(site);
        ret.append(KEY_SEPARATOR);
        ret.append(ssid);
        ret.append(KEY_SEPARATOR);
        ret.append(eventName);

        return ret.toString().toUpperCase();
    }

    /**
     * returns <code>true</code> if the event defined in {@param studySubjectWithEventsType} is
     * present present in this ClinicalData.
     *
     * @param studySubjectWithEventsType
     * @return returns <code>true</code> if the event defined in {@param studySubjectWithEventsType} is
     * present present in this ClinicalData.
     */
    public boolean isEventPresent(StudySubjectWithEventsType studySubjectWithEventsType) {
        String subjectID = studySubjectWithEventsType.getLabel();
        if ((subjectID != null) && (! subjectID.equals(ssid))) {
            return false;
        }
        String studyIdentifier = studySubjectWithEventsType.getStudyRef().getIdentifier();
        String siteIdentifier =
                studySubjectWithEventsType.getStudyRef().getSiteRef() != null ? studySubjectWithEventsType.getStudyRef().getSiteRef().getIdentifier() : "";
        if ((studyIdentifier != null) && (! studyIdentifier.equals(study))) {
            return false;
        }
        if (StringUtils.isNotEmpty(siteIdentifier) && (! siteIdentifier.equals(site))) {
            return false;
        }

        EventsType events = studySubjectWithEventsType.getEvents();
        List<String> clinicalAxis =
                events.getEvent().stream().
                        map(eventResponseType -> eventResponseType.getEventDefinitionOID() + eventResponseType.getOccurrence())
                        .collect(Collectors.toList());
        String compareValue = this.getEventName() + this.getEventRepeat();
        return clinicalAxis.contains(compareValue);
    }

    public String createODMGroupingKey() {
        return ssid;
    }

    public List<String> getValues(Boolean isMultiSelect) {
        if( (isMultiSelect == null) || (isMultiSelect == false)) {
            List<String> ret = new ArrayList<>();
            ret.add(value);
            return ret;
        }
        String[] split = value.split(",", -1); // -1 means we will not discard empty values, e.g ,,
        List<String> values = Arrays.asList(split);
        return values;

    }

    public String toOffenderString() {
        String groupRepeatPart;
        if (groupRepeat != null) {
            groupRepeatPart = " item group repeat " + groupRepeat;
        } else {
            groupRepeatPart = " non-repeating group";
        }
        String valueStr = StringUtils.isEmpty(value) ? "[EMPTY_VALUE]" : value;
        String offenderMsg = "Line number [" +  + lineNumber + "]. "
                + "Subject " + CD_SEP_PREFIX + ssid + CD_SEP_POSTEFIX
                + " item " + CD_SEP_PREFIX + item + CD_SEP_POSTEFIX
                + CD_SEP_PREFIX  + groupRepeatPart + CD_SEP_POSTEFIX
                + " in CRF "  + CD_SEP_PREFIX + crfName + CD_SEP_POSTEFIX
                + " version "  + CD_SEP_PREFIX + crfVersion + CD_SEP_POSTEFIX
                + " in event "  + CD_SEP_PREFIX + eventName + CD_SEP_POSTEFIX
                + " event repeat "  + CD_SEP_PREFIX + eventRepeat + CD_SEP_POSTEFIX
                + " value " +  CD_SEP_PREFIX + "<b>" + valueStr + "</b>";
        String closingPoint = StringUtils.removeEnd(CD_SEP_POSTEFIX, ", ") + ".";
        offenderMsg += closingPoint;
        return offenderMsg;
    }

    public void convertValueToISO_8601() {
        String day = StringUtils.substringBefore(value, "-");
        String month = StringUtils.substringBetween(value, "-", "-");
        String year = StringUtils.substringAfterLast(value, "-");
        value = year + "-" + month + "-" + day;
    }

    public String getOriginalItem() {
        return originalItem;
    }

    public void setOriginalItem(String originalItem) {
        this.originalItem = originalItem;
    }

}
