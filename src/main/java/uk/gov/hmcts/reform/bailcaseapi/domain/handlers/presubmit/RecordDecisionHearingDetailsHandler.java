package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.HearingDetailsFields;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.IaHearingsApiService;

/**
 * Sends the hearing details recorded with the decision, along with the rest of the case (including the judge
 * name), to ia-hearings-api, which records them in HMC.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecordDecisionHearingDetailsHandler implements PreSubmitCallbackHandler<BailCase> {

    private final IaHearingsApiService iaHearingsApiService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.RECORD_THE_DECISION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        boolean hearingSelected = bailCase.read(ATTENDEES_AND_DURATION_HEARING_ID, String.class)
            .filter(hearingId -> !hearingId.isBlank())
            .or(() -> HearingDetailsFields.selectedCode(bailCase, ATTENDEES_AND_DURATION_HEARING))
            .isPresent();

        if (!hearingSelected) {
            log.info("No hearing selected for case {}, hearing details not sent to HMC",
                     callback.getCaseDetails().getId());
            return new PreSubmitCallbackResponse<>(bailCase);
        }

        BailCase updatedBailCase = iaHearingsApiService.aboutToSubmit(callback);

        // once HMC has them the details are not kept, so a later decision on this case cannot send them again
        HearingDetailsFields.clear(updatedBailCase);

        return new PreSubmitCallbackResponse<>(updatedBailCase);
    }
}
