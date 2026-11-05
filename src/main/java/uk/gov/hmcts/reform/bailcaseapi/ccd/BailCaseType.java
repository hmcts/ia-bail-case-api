package uk.gov.hmcts.reform.bailcaseapi.ccd;

import static java.lang.System.getenv;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.bailcaseapi.ccd.hook.CurrentCaseStateHook;
import uk.gov.hmcts.reform.bailcaseapi.ccd.hook.DataFixingHook;
import uk.gov.hmcts.reform.bailcaseapi.ccd.hook.EventRoleCheckHook;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.State;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.UserRole;

/**
 * The case-type and jurisdiction identity, definition flags and banner for {@code Bail}, and the
 * hooks that run around every SDK-served callback.
 */
@Component
@RequiredArgsConstructor
public class BailCaseType implements CCDConfig<CaseData, State, UserRole> {

    private final DataFixingHook dataFixingHook;
    private final EventRoleCheckHook eventRoleCheckHook;
    private final CurrentCaseStateHook currentCaseStateHook;

    /**
     * Configures the case-type and jurisdiction identity, definition flags and banner for {@code Bail}.
     *
     * @param builder the config builder
     */
    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> builder) {
        builder.setCallbackHost(getenv().getOrDefault("CCD_DEF_BAIL_URL", "http://localhost:8686"));
        builder.caseType("Bail", "Bail${CCD_DEF_VERSION}", "Bail Case");
        builder.jurisdiction("IA", "Immigration & Asylum", "Immigration & Asylum");
        builder.jurisdictionShuttered();
        builder.noCaseHistoryTab();
        builder.explicitStateGrants();
        builder.tabsRespectDeclaredFieldAccess();
        builder.omitHistoryForRoles(UserRole.values());

        builder.addPreEventHook(dataFixingHook);
        builder.addPostEventHook(eventRoleCheckHook);
        builder.addPostEventHook(currentCaseStateHook);
    }
}
