package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.FeatureToggleService;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;

@Slf4j
@Component
public class SubmitApplicationSubmitHandler implements PreSubmitCallbackStateHandler<BailCase> {

    private final FeatureToggleService featureToggleService;

    public SubmitApplicationSubmitHandler(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.SUBMIT_APPLICATION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback,
                                                      PreSubmitCallbackResponse<BailCase> callbackResponse) {

        requireNonNull(callbackResponse, "callback must not be null");

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        YesOrNo isBailsLocationReferenceDataEnabled = featureToggleService.locationRefDataEnabled() ? YES : NO;
        bailCase.write(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, isBailsLocationReferenceDataEnabled);

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
