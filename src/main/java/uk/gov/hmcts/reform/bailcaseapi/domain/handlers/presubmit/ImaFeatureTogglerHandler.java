package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.FeatureToggler;

@Service
public class ImaFeatureTogglerHandler {

    private final FeatureToggler featureToggler;

    public ImaFeatureTogglerHandler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    public boolean isImaEnabled() {
        return featureToggler.getValue("ima-feature-flag", false);
    }
}
