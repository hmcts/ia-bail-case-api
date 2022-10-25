package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import java.util.Set;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.TTL;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.ccddataservice.TimeToLiveDataService;

public class RecordTheDecisionConfirmation implements PostSubmitCallbackHandler<BailCase> {

    private final TimeToLiveDataService timeToLiveDataService;

    public RecordTheDecisionConfirmation(TimeToLiveDataService timeToLiveDataService) {
        this.timeToLiveDataService = timeToLiveDataService;
    }

    @Override
    public boolean canHandle(Callback<BailCase> callback) {
        return (callback.getEvent() == Event.RECORD_THE_DECISION);
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<BailCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        // CCD doesn't set the "ttl.suspended" to NO (clock is active) unless it's null.
        // When the clock has been stopped "ttl.suspended" gets populated with "YES" and isn't null anymore
        // So it requires manual intervention to set "ttl.suspended = NO" when starting the clock again
        if (wasSuccessful(callback) && !isClockActive(bailCase)) {
            // stop the clock
            timeToLiveDataService.updateTheClock(callback, false);
        }

        return new PostSubmitCallbackResponse();
    }

    private boolean wasSuccessful(Callback<BailCase> callback) {
        CaseDetails<BailCase> caseDetails = callback.getCaseDetails();
        return caseDetails.getState().equals(State.UNSIGNED_DECISION);
    }

    private boolean isClockActive(BailCase bailCase) {
        return bailCase.read(BailCaseFieldDefinition.TTL, TTL.class)
            .map(ttl -> ttl.getSuspended().equals(YesOrNo.NO))
            .orElse(false);
    }

}
