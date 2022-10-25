package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.TTL;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.ccddataservice.TimeToLiveDataService;

@Component
public class EndApplicationConfirmation implements PostSubmitCallbackHandler<BailCase> {

    private final TimeToLiveDataService timeToLiveDataService;

    public EndApplicationConfirmation(TimeToLiveDataService timeToLiveDataService) {
        this.timeToLiveDataService = timeToLiveDataService;
    }

    @Override
    public boolean canHandle(
        Callback<BailCase> callback
    ) {

        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.END_APPLICATION;
    }

    @Override
    public PostSubmitCallbackResponse handle(
        Callback<BailCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();
        postSubmitResponse.setConfirmationHeader("# You have ended the application");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
                + "A notification has been sent to all parties. "
                + "No further action is required.<br>"
        );

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        // CCD doesn't set the "ttl.suspended" to NO (clock is active) unless it's null.
        // When the clock has been stopped "ttl.suspended" gets populated with "YES" and isn't null anymore
        // So it requires manual intervention to set "ttl.suspended = NO" when starting the clock again
        if (wasSuccessful(callback) && !isClockActive(bailCase)) {
            // stop the clock
            timeToLiveDataService.updateTheClock(callback, false);
        }

        return postSubmitResponse;
    }

    private boolean wasSuccessful(Callback<BailCase> callback) {
        State caseState = callback.getCaseDetails().getState();
        return caseState.equals(State.APPLICATION_ENDED);
    }

    private boolean isClockActive(BailCase bailCase) {
        return bailCase.read(BailCaseFieldDefinition.TTL, TTL.class)
            .map(ttl -> ttl.getSuspended().equals(YesOrNo.NO))
            .orElse(false);
    }
}
