package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.refdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Jacksonized
public class CourtVenue {

    String siteName;
    String courtName;
    String epimmsId;
    String courtStatus;
    String isHearingLocation;
    String isCaseManagementLocation;
    String courtAddress;
    String postcode;
    String locationType;

    public CourtVenue(
        String siteName,
        String courtName,
        String epimmsId,
        String courtStatus,
        String isHearingLocation,
        String isCaseManagementLocation,
        String courtAddress,
        String postcode,
        String locationType
    ) {
        this.siteName = siteName;
        this.courtName = courtName;
        this.epimmsId = epimmsId;
        this.courtStatus = courtStatus;
        this.isHearingLocation = isHearingLocation;
        this.isCaseManagementLocation = isCaseManagementLocation;
        this.courtAddress = courtAddress;
        this.postcode = postcode;
        this.locationType = locationType == null ? "" : locationType;
    }
}
