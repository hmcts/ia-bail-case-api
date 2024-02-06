package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.FeatureToggler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImaFeatureTogglerHandlerTest {
    @Mock
    private FeatureToggler featureToggler;

    private ImaFeatureTogglerHandler imaFeatureTogglerHandler;

    @BeforeEach
    public void setUp() {
        imaFeatureTogglerHandler = new ImaFeatureTogglerHandler(featureToggler);
    }

    @Test
    void handler_checks_age_assessment_feature_flag_set_value() {
        when(featureToggler.getValue("ima-feature-flag", false)).thenReturn(true);
        assertTrue(imaFeatureTogglerHandler.isImaEnabled());
    }

}
