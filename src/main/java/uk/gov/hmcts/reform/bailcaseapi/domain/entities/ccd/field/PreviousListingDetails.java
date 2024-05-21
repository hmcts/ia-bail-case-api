package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingHearingCentre;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class PreviousListingDetails {


    @Getter
    private ListingEvent listingEvent;
    @Getter
    private ListingHearingCentre listingLocation;
    @Getter
    private String listingHearingDate;
    @Getter
    private String listingHearingDuration;

    private PreviousListingDetails() {
        // noop -- for deserializer
    }

    public PreviousListingDetails(
        ListingEvent listingEvent,
        ListingHearingCentre listingLocation,
        String listingHearingDate,
        String listingHearingDuration
    ) {
        requireNonNull(listingEvent);
        requireNonNull(listingLocation);
        requireNonNull(listingHearingDate);
        requireNonNull(listingHearingDuration);

        this.listingEvent = listingEvent;
        this.listingLocation = listingLocation;
        this.listingHearingDate = listingHearingDate;
        this.listingHearingDuration = listingHearingDuration;
    }
}
