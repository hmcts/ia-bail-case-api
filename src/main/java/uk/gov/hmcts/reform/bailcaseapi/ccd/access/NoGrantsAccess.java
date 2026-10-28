package uk.gov.hmcts.reform.bailcaseapi.ccd.access;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import uk.gov.hmcts.ccd.sdk.api.HasAccessControl;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Permission;

/**
 * For the fields the hand-written AuthorisationCaseField definition grants to nobody. Naming this
 * keeps them out of the read a tab would otherwise inject.
 */
public class NoGrantsAccess implements HasAccessControl {
    @Override
    public SetMultimap<HasRole, Permission> getGrants() {
        return HashMultimap.create();
    }
}
