package uk.gov.hmcts.reform.bailcaseapi.ccd.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.reform.bailcaseapi.ccd.model.UserRole;

/**
 * Generated from the hand-written AuthorisationCaseField definition — do not edit by hand.
 */
public class AdmofficerApproverCaaLegalrepSolicitorSystemCrudHomeofficebailHomeofficepouIacjudgeRAccess implements HasAccessControl {
    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        SetMultimap<HasRole, Permission> grants = HashMultimap.create();
        grants.put(UserRole.CASEWORKER_APPROVER, Permission.C);
        grants.put(UserRole.CASEWORKER_APPROVER, Permission.R);
        grants.put(UserRole.CASEWORKER_APPROVER, Permission.U);
        grants.put(UserRole.CASEWORKER_APPROVER, Permission.D);
        grants.put(UserRole.CASEWORKER_CAA, Permission.C);
        grants.put(UserRole.CASEWORKER_CAA, Permission.R);
        grants.put(UserRole.CASEWORKER_CAA, Permission.U);
        grants.put(UserRole.CASEWORKER_CAA, Permission.D);
        grants.put(UserRole.CASEWORKER_IA_ADMOFFICER, Permission.C);
        grants.put(UserRole.CASEWORKER_IA_ADMOFFICER, Permission.R);
        grants.put(UserRole.CASEWORKER_IA_ADMOFFICER, Permission.U);
        grants.put(UserRole.CASEWORKER_IA_ADMOFFICER, Permission.D);
        grants.put(UserRole.CASEWORKER_IA_HOMEOFFICEBAIL, Permission.R);
        grants.put(UserRole.CASEWORKER_IA_HOMEOFFICEPOU, Permission.R);
        grants.put(UserRole.CASEWORKER_IA_IACJUDGE, Permission.R);
        grants.put(UserRole.CASEWORKER_IA_LEGALREP_SOLICITOR, Permission.C);
        grants.put(UserRole.CASEWORKER_IA_LEGALREP_SOLICITOR, Permission.R);
        grants.put(UserRole.CASEWORKER_IA_LEGALREP_SOLICITOR, Permission.U);
        grants.put(UserRole.CASEWORKER_IA_LEGALREP_SOLICITOR, Permission.D);
        grants.put(UserRole.CASEWORKER_IA_SYSTEM, Permission.C);
        grants.put(UserRole.CASEWORKER_IA_SYSTEM, Permission.R);
        grants.put(UserRole.CASEWORKER_IA_SYSTEM, Permission.U);
        grants.put(UserRole.CASEWORKER_IA_SYSTEM, Permission.D);
        return grants;
    }
}
