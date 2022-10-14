package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.TTL;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CASE_TYPE_TTL;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.MAKE_NEW_APPLICATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.MANAGE_CASE_TTL;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;


@Component
public class ManageCaseTTLHandler implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT)
            && (callback.getEvent().equals(MANAGE_CASE_TTL)
            || callback.getEvent().equals(MAKE_NEW_APPLICATION));
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        TTL caseTypeTTL = bailCase
            .read(CASE_TYPE_TTL, TTL.class)
            .orElseThrow(() -> new IllegalStateException("caseTypeTTL is not present"));

        if (callback.getEvent().equals(MAKE_NEW_APPLICATION)) {
            caseTypeTTL.setSuspended(YesOrNo.YES);
            bailCase.write(CASE_TYPE_TTL, caseTypeTTL);
        }

        //bailCase.clear(CASE_TYPE_TTL);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

}
