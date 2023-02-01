package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CASE_LEVEL_FLAGS;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;


@Component
class CreateFlagHandler implements PreSubmitCallbackHandler<BailCase> {

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.START_APPLICATION;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        final String applicantFullName =
                asylumCase
                        .read(APPLICANT_FULL_NAME, String.class)
                        .orElseThrow(() -> new IllegalStateException("applicantFullName is not present"));

        asylumCase.write(APPELLANT_LEVEL_FLAGS, new StrategicCaseFlag(applicantFullName));
        asylumCase.write(CASE_LEVEL_FLAGS, new StrategicCaseFlag());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
