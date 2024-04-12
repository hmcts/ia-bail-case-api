package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.refdata.CourtLocationCategory;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.refdata.CourtVenue;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.refdata.LocationRefDataApi;

@Service
@RequiredArgsConstructor
public class LocationRefDataService {

    private final AuthTokenGenerator authTokenGenerator;
    private final UserDetails userDetails;
    private final LocationRefDataApi locationRefDataApi;
    @org.springframework.beans.factory.annotation.Value("${hmcts_service_id}")
    private String serviceId;

    public DynamicList getHearingLocationsDynamicList() {

        return new DynamicList(new Value("", ""), getCourtVenues().stream()
            .map(courtVenue -> new Value(courtVenue.getEpimmsId(), courtVenue.getCourtName()))
            .toList());
    }

    private List<CourtVenue> getCourtVenues() {

        CourtLocationCategory locationCategory = locationRefDataApi
            .getCourtVenues(userDetails.getAccessToken(), authTokenGenerator.generate(), serviceId);

        return locationCategory == null
            ? Collections.emptyList()
            : locationCategory.getCourtVenues();
    }
}
