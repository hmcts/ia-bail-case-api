package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdCaseAssignment;

@Slf4j
@Component
public class ChangeRepresentationConfirmation implements PostSubmitCallbackHandler<BailCase> {

    private final CcdCaseAssignment ccdCaseAssignment;

    public ChangeRepresentationConfirmation(
        CcdCaseAssignment ccdCaseAssignment
    ) {

        this.ccdCaseAssignment = ccdCaseAssignment;
    }

    public boolean canHandle(
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        return (callback.getEvent() == Event.NOC_REQUEST);
    }

    /**
     * the confirmation message and the error message are coming from ExUI and cannot be customised.
     */
    public PostSubmitCallbackResponse handle(
        Callback<BailCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        try {
            ccdCaseAssignment.applyNoc(callback);

            // redundant IF-statement for now, but there'll be more IF-statements when removal of LR gets implemented
            if (callback.getEvent() == Event.NOC_REQUEST) {

                String caseReference = callback.getCaseDetails().getCaseData()
                    .read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse("");

                postSubmitResponse.setConfirmationHeader(
                    "# You're now representing a client on case " + caseReference
                );
            }
        } catch (Exception e) {
            log.error("Unable to change representation (apply noc) for case id {} with error message: {}",
                      callback.getCaseDetails().getId(), e.getMessage());

            postSubmitResponse.setConfirmationBody(
                "### Something went wrong\n\n"
                + "You have not stopped representing the appellant in this appeal.\n\n"
                + "Use the [stop representing a client](/case/IA/Bail/"
                + callback.getCaseDetails().getId()
                + "/trigger/removeRepresentation/removeRepresentationSingleFormPageWithComplex) feature to try again."
            );
        }

        return postSubmitResponse;
    }
}
