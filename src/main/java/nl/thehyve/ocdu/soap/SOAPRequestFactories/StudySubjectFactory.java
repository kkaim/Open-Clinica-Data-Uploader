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

package nl.thehyve.ocdu.soap.SOAPRequestFactories;

import nl.thehyve.ocdu.models.OCEntities.Study;
import nl.thehyve.ocdu.models.OCEntities.Subject;
import nl.thehyve.ocdu.models.OcDefinitions.SiteDefinition;
import nl.thehyve.ocdu.validators.UtilChecks;
import org.apache.commons.lang3.StringUtils;
import org.openclinica.ws.beans.GenderType;
import org.openclinica.ws.beans.StudyRefType;
import org.openclinica.ws.beans.StudySubjectType;
import org.openclinica.ws.beans.SubjectType;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static nl.thehyve.ocdu.soap.SOAPRequestFactories.StudyRefFactory.createStudyRef;

/**
 * Factory for a StudySubject.
 * Created by piotrzakrzewski on 17/06/16.
 */
public class StudySubjectFactory {

    public static StudySubjectType createStudySubject(Subject subject, Study study, SiteDefinition site) {
        StudySubjectType studySubjectType = new StudySubjectType();
        SubjectType subjectType = createSubjectType(subject);
        studySubjectType.setSubject(subjectType);
        studySubjectType.setLabel(subject.getSsid());
        StudyRefType studyRef = createStudyRef(study, site);
        studySubjectType.setStudyRef(studyRef);
        if (subject.getDateOfEnrollment() == null || subject.getDateOfEnrollment().equals("")) {
            studySubjectType.setEnrollmentDate(getNowDate());
        } else {
            studySubjectType.setEnrollmentDate(createXMLGregorianDate(subject.getDateOfEnrollment()));
        }
        return studySubjectType;
    }

    public static SubjectType createSubjectType(Subject subject) {
        SubjectType subjectType = new SubjectType();
        if (StringUtils.isNotEmpty(subject.getDateOfBirth())) {
            if (isYearOnly(subject.getDateOfBirth())) {
                subjectType.setYearOfBirth(BigInteger.valueOf(Integer.parseInt(subject.getDateOfBirth())));
            } else {
                XMLGregorianCalendar dateOfBirth = createXMLGregorianDate(subject.getDateOfBirth());
                subjectType.setDateOfBirth(dateOfBirth);
            }
        }
        if (StringUtils.isNotEmpty(subject.getPersonId())) {
            subjectType.setUniqueIdentifier(subject.getPersonId());
        }
        if (subject.getGender() != null && !subject.getGender().equals("")) {
            GenderType genderType = GenderType.fromValue(subject.getGender());
            subjectType.setGender(genderType);
        }
        return subjectType;
    }

    public static XMLGregorianCalendar createXMLGregorianDate(String dateOfBirthString) {
        try { //TODO: unify OC date format checking in the application
            GregorianCalendar cal = new GregorianCalendar();
            String dateFormat;
            boolean fullDate;
            if (isYearOnly(dateOfBirthString)) {
                dateFormat = "yyyy";
                fullDate = false;
            } else {
                dateFormat = "dd-MM-yyyy";
                fullDate = true;
            }
            if (fullDate && !UtilChecks.isDate(dateOfBirthString)) {
                return null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            Date date = df.parse(dateOfBirthString);
            cal.setTime(date);
            XMLGregorianCalendar dateOfBirth;
            if (fullDate)
                dateOfBirth = getFullXmlDate(cal);
            else dateOfBirth = getYearOnlyXmlDate(cal);
            return dateOfBirth;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static XMLGregorianCalendar getFullXmlDate(GregorianCalendar cal) {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendarDate(cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static XMLGregorianCalendar getYearOnlyXmlDate(GregorianCalendar cal) {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendarDate(cal.get(Calendar.YEAR),
                    DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
                    DatatypeConstants.FIELD_UNDEFINED);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static XMLGregorianCalendar getNowDate() {
        Date now = new Date();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(now);
        XMLGregorianCalendar xmlDate = null;
        try {
            xmlDate = getFullXmlDate(cal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xmlDate;
    }

    private static boolean isYearOnly(String dateString) {
        return (dateString.length() == 4);
    }
}
