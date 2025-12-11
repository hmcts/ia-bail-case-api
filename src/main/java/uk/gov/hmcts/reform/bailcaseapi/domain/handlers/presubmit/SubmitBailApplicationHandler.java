package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdSupplementaryUpdater;

@Component
public class SubmitBailApplicationHandler implements PreSubmitCallbackHandler<BailCase> {

    private final CcdSupplementaryUpdater ccdSupplementaryUpdater;

    public SubmitBailApplicationHandler(CcdSupplementaryUpdater ccdSupplementaryUpdater) {
        this.ccdSupplementaryUpdater = ccdSupplementaryUpdater;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        return callback.getEvent() == Event.SUBMIT_APPLICATION
            && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        boolean hasUpdated = ccdSupplementaryUpdater.setHmctsServiceIdSupplementary(callback);

        PreSubmitCallbackResponse<BailCase> response =
            new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());

        if (!hasUpdated) {
            response.addError("Failed to update HMCTS Service ID supplementary data. Please try again.");
        }
        return response;
    }
}
