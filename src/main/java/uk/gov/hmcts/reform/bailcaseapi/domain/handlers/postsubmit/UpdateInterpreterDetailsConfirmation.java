package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;

@Component
public class UpdateInterpreterDetailsConfirmation implements PostSubmitCallbackHandler<BailCase> {

    public boolean canHandle(Callback<BailCase> callback) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.UPDATE_INTERPRETER_DETAILS;
    }

    public PostSubmitCallbackResponse handle(Callback<BailCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();

        String hearingsTabUrl = "/case/IA/Bail/" + callback.getCaseDetails().getId() + "#Hearing%20and%20appointment";

        postSubmitResponse.setConfirmationHeader("# Interpreter details have been updated");
        postSubmitResponse.setConfirmationBody(
            "#### What happens next\n\n"
                + "You now need to update the hearing in the "
                + "[Hearings tab](" + hearingsTabUrl + ")"
                + " to ensure the new interpreter information is displayed in List Assist."
                + "\n\nIf updates need to be made to the interpreter booking status this should be completed"
                + " before updating the hearing."
        );

        return postSubmitResponse;
    }
}
