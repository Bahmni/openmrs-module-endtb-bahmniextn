package org.openmrs.module.bahmniendtb.dataintegrity.rules;

import org.openmrs.module.dataintegrity.rule.RuleDefn;
import org.openmrs.module.dataintegrity.rule.RuleResult;
import org.openmrs.Concept;
import org.openmrs.PatientProgram;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniendtb.dataintegrity.rules.helper.MissingValuesHelper;

import java.util.Arrays;
import java.util.List;

import static org.openmrs.module.bahmniendtb.EndTBConstants.*;

public class MissingCaseDefinitionViolation implements RuleDefn<PatientProgram> {

    private ConceptService conceptService;

    private MissingValuesHelper missingValuesHelper;

    public MissingCaseDefinitionViolation(){
        conceptService = Context.getConceptService();
        missingValuesHelper = Context.getRegisteredComponent("missingValuesHelper", MissingValuesHelper.class);
    }

    public MissingCaseDefinitionViolation(MissingValuesHelper missingDatesHelper, ConceptService conceptService) {
        this.missingValuesHelper = missingDatesHelper;
        this.conceptService = conceptService;
    }

    @Override
    public List<RuleResult<PatientProgram>> evaluate() {

        Concept whoGroupQuestion = conceptService.getConceptByName(BASELINE_CASEDEFINITION_WHO_GROUP);
        Concept siteDateQuestion = conceptService.getConceptByName(BASELINE_CASEDEFINITION_DISEASE_SITE);
        Concept methodQuestion = conceptService.getConceptByName(BASELINE_CASEDEFINITION_CONFIRMATION_METHOD);

        return missingValuesHelper
            .getMissingObsInObsSetViolations(BASELINE_FORM, BASELINE_CASEDEFINITION_WHO_GROUP, BASELINE_DEFAULT_COMMENT,
                    Arrays.asList(whoGroupQuestion, siteDateQuestion, methodQuestion));
    }
}


