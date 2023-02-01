package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdSupplementaryUpdater;

import java.util.Map;

@Component
public class BailSupplementaryDataFixingHandler implements PreSubmitCallbackHandler<BailCase> {

    private final CcdSupplementaryUpdater ccdSupplementaryUpdater;

    public BailSupplementaryDataFixingHandler(CcdSupplementaryUpdater ccdSupplementaryUpdater) {
        this.ccdSupplementaryUpdater = ccdSupplementaryUpdater;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<BailCase> callback
    ) {
        return true;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<BailCase> callback
    ) {
        final CaseDetails<BailCase> caseDetails = callback.getCaseDetails();
        final BailCase bailCase = caseDetails.getCaseData();

        Map<String, JsonNode> supplementaryData = caseDetails.getSupplementaryData();

        if (supplementaryData == null || !supplementaryData.containsKey(CcdSupplementaryUpdater.HMCTS_SERVICE_ID)) {
            ccdSupplementaryUpdater.setHmctsServiceIdSupplementary(callback);
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }

}
