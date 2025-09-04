package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Region {
    NATIONAL("1");

    @JsonValue
    private final String id;

    Region(String id) {
        this.id = id;
    }
}
