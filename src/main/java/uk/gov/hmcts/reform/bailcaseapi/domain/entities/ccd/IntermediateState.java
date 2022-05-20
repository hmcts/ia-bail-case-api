package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * These enums are the intermediate states which are not being displayed as separate states.
 * For ex: From Record_Decision event case can go from decided or ConditionalBail on UI but
 * from UPLOAD_SIGNED_DECISION_NOTICE the state will not change. But we need to set the
 * intermediate state in order to show/hide the fields in the tab.
 */
public enum IntermediateState {

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    IntermediateState(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
