package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_APPLICANT_DOCS_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_APPLICANT_INFO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_CASE_NOTES_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_DECISION_DETAILS_LABEL;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_DIRECTION_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_COMMITMENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_SUPPORTER1;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_SUPPORTER2;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_SUPPORTER3;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_SUPPORTER4;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_GROUNDS_FOR_BAIL;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_HEARING_REQ_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_LEGAL_REP_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_PERSONAL_INFO_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_SUBMISSION_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.VIEW_PREVIOUS_APPLICATIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ShowPreviousApplicationSubmitHandler implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == ABOUT_TO_SUBMIT && callback.getEvent() == VIEW_PREVIOUS_APPLICATIONS;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        bailCase.remove(PREV_APP_SUBMISSION_DETAILS);
        bailCase.remove(PREV_APP_HEARING_REQ_DETAILS);
        bailCase.remove(PREV_APP_APPLICANT_DOCS_DETAILS);
        bailCase.remove(PREV_APP_DECISION_DETAILS_LABEL);
        bailCase.remove(PREV_APP_DIRECTION_DETAILS);
        bailCase.remove(PREV_APP_CASE_NOTES_DETAILS);
        bailCase.remove(PREV_APP_PERSONAL_INFO_DETAILS);
        bailCase.remove(PREV_APP_APPLICANT_INFO);
        bailCase.remove(PREV_APP_FINANCIAL_COND_COMMITMENT);
        bailCase.remove(PREV_APP_FINANCIAL_COND_SUPPORTER1);
        bailCase.remove(PREV_APP_FINANCIAL_COND_SUPPORTER2);
        bailCase.remove(PREV_APP_FINANCIAL_COND_SUPPORTER3);
        bailCase.remove(PREV_APP_FINANCIAL_COND_SUPPORTER4);
        bailCase.remove(PREV_APP_GROUNDS_FOR_BAIL);
        bailCase.remove(PREV_APP_LEGAL_REP_DETAILS);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

}
