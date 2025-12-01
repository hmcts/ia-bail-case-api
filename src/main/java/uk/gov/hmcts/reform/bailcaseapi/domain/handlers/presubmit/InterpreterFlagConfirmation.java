package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.CcdDataService;

@Slf4j
@Component
class InterpreterFlagConfirmation implements PostSubmitCallbackHandler<BailCase> {

    private final CcdDataService ccdDataService;

    public InterpreterFlagConfirmation(
        CcdDataService ccdDataService
    ) {
        this.ccdDataService = ccdDataService;
    }

    @Override
    public boolean canHandle(
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.CREATE_FLAG
            || callback.getEvent() == Event.MANAGE_FLAGS;
    }

    public PostSubmitCallbackResponse handle(
        Callback<BailCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();

        try {
            ccdDataService.setActiveInterpreterFlag(callback);
        } catch (Exception e) {
            log.error(
                "Unable to trigger event to set has interpreter active flag field {} with error message: {}",
                callback.getCaseDetails().getId(), e.getMessage()
            );

            postSubmitResponse.setConfirmationBody(
                "### Something went wrong\n\n"
            );
        }

        return postSubmitResponse;
    }
}
