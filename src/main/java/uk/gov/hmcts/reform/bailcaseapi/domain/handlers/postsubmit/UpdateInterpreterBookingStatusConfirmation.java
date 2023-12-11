package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class UpdateInterpreterBookingStatusConfirmation implements PostSubmitCallbackHandler<BailCase> {

    @Override
    public boolean canHandle(Callback<BailCase> callback) {
        requireNonNull(callback, "callback must not be null");
        return (callback.getEvent() == Event.UPDATE_INTERPRETER_BOOKING_STATUS);
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<BailCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# Booking statuses have been updated");

        postSubmitResponse.setConfirmationBody(
            "### What happens next\n\n"
            + "You now need to update the hearing in the [Hearings tab](/cases/case-details/"
            + callback.getCaseDetails().getId()
            + "/hearings) to ensure the update is displayed in List Assist.\n\nIf an interpreter status has been moved to booked, "
            + "or has been cancelled, ensure that the interpreter details are up to date before updating the hearing."
        );

        return postSubmitResponse;
    }
}
