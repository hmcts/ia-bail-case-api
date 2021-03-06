package uk.gov.hmcts.reform.bailcaseapi.domain.handlers;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

public interface PreSubmitCallbackStateHandler<T extends CaseData> {

    boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback
    );

    PreSubmitCallbackResponse<T> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback,
        PreSubmitCallbackResponse<T> callbackResponse
    );
}
