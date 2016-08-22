package org.openmrs.module.bahmniendtb.dataintegrity.rules.helper;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.PatientProgram;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniendtb.dataintegrity.service.DataintegrityRuleService;
import org.openmrs.module.bahmniendtb.dataintegrity.service.EndTBObsService;
import org.openmrs.module.dataintegrity.rule.RuleResult;
import org.openmrs.module.episodes.Episode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.openmrs.module.bahmniendtb.EndTBConstants.*;

@Component
public class CultureStatusAtStartMissingHelper {

    private EpisodeHelper episodeHelper;

    @Autowired
    public CultureStatusAtStartMissingHelper(EpisodeHelper episodeHelper) {
        this.episodeHelper = episodeHelper;
    }


    public List<RuleResult<PatientProgram>> fetchCultureStatusAtStartMissing(List<Concept> concepts) {
        List<RuleResult<PatientProgram>> patientPrograms = new ArrayList<>();
        Map<Episode, List<Obs>> episodeObsMap = episodeHelper.retrieveAllEpisodesWithObs(concepts);
        for (Map.Entry<Episode, List<Obs>> episodeObsMapEntry : episodeObsMap.entrySet()) {
            if (hasInvalidCultureStatusForGivenTimePeriod(episodeObsMapEntry.getValue())) {
                patientPrograms.add(convertToPatientProgram(episodeObsMapEntry.getKey(), BASELINE_FORM, BASELINE_CASEDEFINITION_MDR_TB_DIAGNOSIS_METHOD, CULTURE_STATUS_MISSING_DEFAULT_COMMENT));
            }
        }
        return patientPrograms;
    }

    private RuleResult<PatientProgram> convertToPatientProgram(Episode episode, String parentConcept, String notesConceptName, String defaultNoteComment) {
        return episodeHelper.mapEpisodeToPatientProgram(episode, parentConcept, notesConceptName, defaultNoteComment);
    }

    private boolean hasInvalidCultureStatusForGivenTimePeriod(List<Obs> obsList) {
        Date treatmentInitiationStartDate = null;
        Date bacteriologySpecimenCollectionDate = null;
        String mtbConfirmation = null;
        String bacteriologyCultureResults = null;
        for (Obs obs : obsList) {
            if (obs.getConcept().getName().getName().equals(TI_START_DATE)) {
                treatmentInitiationStartDate = obs.getValueDatetime();
            } else if (obs.getConcept().getName().getName().equals(BACTERIOLOGY_SPECIMEN_COLLECTION_DATE)) {
                bacteriologySpecimenCollectionDate = obs.getValueDatetime();
            } else if (obs.getConcept().getName().getName().equals(BASELINE_CASEDEFINITION_MDR_TB_DIAGNOSIS_METHOD)) {
                mtbConfirmation = obs.getValueCoded().getDisplayString();
            } else if (obs.getConcept().getName().getName().equals(BACTERIOLOGY_CULTURE_RESULTS)) {
                bacteriologyCultureResults = obs.getValueCoded().getDisplayString();
            }
        }
        if(BACTERIOLOGICALLY_CONFIRMED.equals(mtbConfirmation) && treatmentInitiationStartDate != null) {
            bacteriologySpecimenCollectionDate = bacteriologySpecimenCollectionDate == null ? new Date() : bacteriologySpecimenCollectionDate;
            long dayDifference = getDateDiff(treatmentInitiationStartDate, bacteriologySpecimenCollectionDate, TimeUnit.DAYS);
            if (dayDifference >= 30.5) {
                return true;
            }
        }
        return false;
    }

    private long getDateDiff(Date startDate, Date stopDate, TimeUnit timeUnit) {
        long diffInMillies = stopDate.getTime() - startDate.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
}
