package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.ccddataservice.TimeToLiveDataService;

@Component
public class MakeNewApplicationConfirmation implements PostSubmitCallbackHandler<BailCase> {

    private final TimeToLiveDataService timeToLiveDataService;

    public MakeNewApplicationConfirmation(TimeToLiveDataService timeToLiveDataService) {
        this.timeToLiveDataService = timeToLiveDataService;
    }

    @Override
    public boolean canHandle(Callback<BailCase> callback) {
        return (callback.getEvent() == Event.MAKE_NEW_APPLICATION);
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<BailCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationBody(
            "### What happens next\n\n"
                + "All parties will be notified that the application has been submitted."
        );

        postSubmitResponse.setConfirmationHeader("# You have submitted this application");

        if (wasSuccessful(callback)) {
            // stop the clock
            timeToLiveDataService.updateTheClock(callback, true);
        }

        return postSubmitResponse;
    }

    private boolean wasSuccessful(Callback<BailCase> callback) {
        CaseDetails<BailCase> caseDetails = callback.getCaseDetails();
        return !Set.of(State.APPLICATION_ENDED, State.DECISION_DECIDED).contains(caseDetails.getState());
    }
}
