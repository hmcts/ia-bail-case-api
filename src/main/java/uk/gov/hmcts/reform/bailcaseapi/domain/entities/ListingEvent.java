package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ListingEvent {

    INITIAL_LISTING("initialListing"),
    RELISTING("relisting");

    @JsonValue
    private final String id;

    @JsonCreator
    ListingEvent(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
