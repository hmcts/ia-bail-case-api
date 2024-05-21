package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field;

import lombok.*;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingHearingCentre;

import static java.util.Objects.requireNonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviousListingDetails {

    private ListingEvent listingEvent;
    private ListingHearingCentre listingLocation;
    private String listingHearingDate;
    private String listingHearingDuration;
}
