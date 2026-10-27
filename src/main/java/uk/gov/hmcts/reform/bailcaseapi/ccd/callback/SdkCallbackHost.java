package uk.gov.hmcts.reform.bailcaseapi.ccd.callback;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.UserRole;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;

/**
 * Prefixes the URLs the SDK writes for its own callbacks. An event that registers a callback as a
 * method rather than a URL is published as {@code <host>/callbacks/about-to-submit?eventId=...},
 * and the host has to be the same placeholder the hand-written URLs use so
 * {@code generate-ccd-definition.sh} resolves it per environment.
 *
 * <p>Events that still point at {@code /bail/ccdAboutToSubmit} keep their own URL — the host is
 * only used where no URL was given.
 */
@Component
public class SdkCallbackHost implements CCDConfig<CaseData, State, UserRole> {

    static final String CALLBACK_HOST = "${CCD_DEF_BAIL_URL}";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> builder) {
        builder.setCallbackHost(CALLBACK_HOST);
    }
}
