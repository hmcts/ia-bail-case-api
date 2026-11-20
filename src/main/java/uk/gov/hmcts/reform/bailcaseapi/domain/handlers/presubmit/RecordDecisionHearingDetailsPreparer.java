package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARINGS_AVAILABLE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.HearingDetailsFields;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.BailCaseServiceResponseException;

/**
 * Asks ia-hearings-api for the hearings HMC is awaiting details for. The hearing details pages of the event
 * are only shown when there is one, so the decision can still be recorded for a case with no such hearing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecordDecisionHearingDetailsPreparer implements PreSubmitCallbackHandler<BailCase> {

    private final IaHearingsApiService iaHearingsApiService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
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
        HearingDetailsFields.clear(bailCase);

        try {
            bailCase = iaHearingsApiService.aboutToStart(callback);
        } catch (BailCaseServiceResponseException e) {
            log.error("Could not get the hearings for case {}, hearing details will not be recorded: {}",
                      callback.getCaseDetails().getId(), e.getMessage());
        }

        boolean hearingsAvailable = bailCase.read(ATTENDEES_AND_DURATION_HEARING, DynamicList.class)
            .map(hearings -> hearings.getListItems() != null && !hearings.getListItems().isEmpty())
            .orElse(false);
        bailCase.write(ATTENDEES_AND_DURATION_HEARINGS_AVAILABLE, hearingsAvailable ? YesOrNo.YES : YesOrNo.NO);

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
