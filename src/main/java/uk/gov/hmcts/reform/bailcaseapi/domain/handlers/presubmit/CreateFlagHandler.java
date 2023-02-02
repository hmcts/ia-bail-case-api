package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.StrategicCaseFlag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;


import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPELLANT_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CASE_FLAGS;


@Component
class CreateFlagHandler implements PreSubmitCallbackHandler<BailCase> {

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == Event.CREATE_FLAG;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        Optional<StrategicCaseFlag> existingCaseLevelFlags = bailCase.read(CASE_FLAGS);

        Optional<StrategicCaseFlag> existingAppellentLevelFlags = bailCase.read(APPELLANT_LEVEL_FLAGS);

        if(existingAppellentLevelFlags.isEmpty()
                || existingAppellentLevelFlags.get().getPartyName() == null
                || existingAppellentLevelFlags.get().getPartyName().isBlank()) {
            final String appellantNameForDisplay =
                    bailCase
                            .read(APPLICANT_FULL_NAME, String.class)
                            .orElseThrow(() -> new IllegalStateException("applicantFullName is not present"));

            bailCase.write(APPELLANT_LEVEL_FLAGS, new StrategicCaseFlag(appellantNameForDisplay));
        }

        if(existingCaseLevelFlags.isEmpty()) {
            bailCase.write(CASE_FLAGS, new StrategicCaseFlag());
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
