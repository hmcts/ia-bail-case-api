package uk.gov.hmcts.reform.bailcaseapi.ccd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.befta.dse.ccd.CcdEnvironment;
import uk.gov.hmcts.befta.dse.ccd.CcdRoleConfig;
import uk.gov.hmcts.befta.dse.ccd.DataLoaderToDefinitionStore;
import uk.gov.hmcts.befta.exception.ImportException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HighLevelDataSetupApp extends DataLoaderToDefinitionStore {

    private static final Logger LOG = LoggerFactory.getLogger(HighLevelDataSetupApp.class);

    private static final int GATEWAY_TIMEOUT = 504;
    private static final int CONFLICT = 409;
    private static final Set<Integer> TOLERABLE_STATUS_CODES = Set.of(GATEWAY_TIMEOUT, CONFLICT);

    /**
     * Every role named in a UserRole column of the Bail definition. The definition store
     * rejects an import referencing a role it does not know about, so a definition store
     * stood up from scratch — i.e. preview — needs these created first. Every Bail case
     * field, state and event is classified PUBLIC, so the roles are too.
     */
    private static final List<CcdRoleConfig> CCD_ROLES = List.of(
        new CcdRoleConfig("GS_profile", "PUBLIC"),
        new CcdRoleConfig("caseworker-approver", "PUBLIC"),
        new CcdRoleConfig("caseworker-caa", "PUBLIC"),
        new CcdRoleConfig("caseworker-ia-admofficer", "PUBLIC"),
        new CcdRoleConfig("caseworker-ia-bails", "PUBLIC"),
        new CcdRoleConfig("caseworker-ia-caseofficer", "PUBLIC"),
        new CcdRoleConfig("caseworker-ia-homeofficebail", "PUBLIC"),
        new CcdRoleConfig("caseworker-ia-homeofficepou", "PUBLIC"),
        new CcdRoleConfig("caseworker-ia-iacjudge", "PUBLIC"),
        new CcdRoleConfig("caseworker-ia-internal", "PUBLIC"),
        new CcdRoleConfig("caseworker-ia-legalrep-solicitor", "PUBLIC"),
        new CcdRoleConfig("caseworker-ia-rparobot", "PUBLIC"),
        new CcdRoleConfig("caseworker-ia-system", "PUBLIC"),
        new CcdRoleConfig("cft-ttl-manager", "PUBLIC"),
        new CcdRoleConfig("citizen", "PUBLIC"),
        new CcdRoleConfig("ia_bail_caseflags_profile", "PUBLIC"),
        new CcdRoleConfig("ia_bail_caselinks_profile", "PUBLIC"),
        new CcdRoleConfig("ia_specific_access_admin_profile", "PUBLIC"),
        new CcdRoleConfig("ia_specific_access_ctsc_profile", "PUBLIC"),
        new CcdRoleConfig("ia_specific_access_judge_profile", "PUBLIC"),
        new CcdRoleConfig("ia_specific_access_legal_ops_profile", "PUBLIC")
    );

    public HighLevelDataSetupApp(CcdEnvironment dataSetupEnvironment) {
        super(dataSetupEnvironment);
    }

    public static void main(String[] args) throws Throwable {
        main(HighLevelDataSetupApp.class, args);
    }

    @Override
    public void addCcdRoles() {
        if (getDataSetupEnvironment() != CcdEnvironment.PREVIEW) {
            LOG.info("Skipping CCD role creation for {} - roles already exist there",
                getDataSetupEnvironment());
            return;
        }

        for (CcdRoleConfig roleConfig : CCD_ROLES) {
            LOG.info("Adding CCD role {}", roleConfig);
            addCcdRole(roleConfig);
        }
    }

    @Override
    public void createRoleAssignments() {
        // Bail does not provision AM role assignments as part of the definition import.
    }

    @Override
    protected List<String> getAllDefinitionFilesToLoadAt(String definitionsPath) {
        String environmentName = ((CcdEnvironment) getDataSetupEnvironment()).name().toLowerCase(Locale.UK);
        return List.of("build/ccd-config/ccd-bail-config-" + environmentName + ".xlsx");
    }

    @Override
    protected boolean shouldTolerateDataSetupFailure(Throwable e) {
        // A definition this size regularly exceeds the gateway's 30s timeout but still
        // imports, and re-importing an unchanged definition answers 409.
        if (e instanceof ImportException importException) {
            return TOLERABLE_STATUS_CODES.contains(importException.getHttpStatusCode());
        }

        return false;
    }
}
