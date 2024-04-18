package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.refdata.CourtLocationCategory;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.refdata.CourtVenue;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.refdata.LocationRefDataApi;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LocationRefDataServiceTest {

    @Mock
    private LocationRefDataApi locationRefDataApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private UserDetails userDetails;

    private LocationRefDataService locationRefDataService;

    @BeforeEach
    void setup() {
        List<CourtVenue> courtVenues = List.of(
            CourtVenue.builder()
                .epimmsId("1234")
                .courtName("Manchester")
                .courtStatus("Open")
                .isHearingLocation("Y")
                .isCaseManagementLocation("N")
                .build(),
            CourtVenue.builder()
                .epimmsId("3344")
                .courtName("Birmingham")
                .courtStatus("Open")
                .isHearingLocation("N")
                .isCaseManagementLocation("Y")
                .build(),
            CourtVenue.builder()
                .epimmsId("1234")
                .courtName("Bradford")
                .courtStatus("Closed")
                .isCaseManagementLocation("N")
                .isHearingLocation("N")
                .build()
        );
        CourtLocationCategory courtLocationCategory = CourtLocationCategory
            .builder().courtTypeId("111").courtType("IAC").serviceCode("BFA1").courtVenues(courtVenues).build();
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(userDetails.getAccessToken()).thenReturn("userAccessToken");
        when(locationRefDataApi.getCourtVenues(any(), any(), any()))
            .thenReturn(courtLocationCategory);

        locationRefDataService = new LocationRefDataService(authTokenGenerator, userDetails, locationRefDataApi);
    }

    @Test
    void test_getHearingLocationsDynamicList() {

        DynamicList dynamicList = locationRefDataService.getHearingLocationsDynamicList();

        assertEquals(1, dynamicList.getListItems().size());
        assertEquals("Manchester", dynamicList.getListItems().get(0).getLabel());
        assertEquals("1234", dynamicList.getListItems().get(0).getCode());
    }

    @Test
    void test_getCaseManagementLocationsDynamicList() {

        DynamicList dynamicList = locationRefDataService.getCaseManagementLocationsDynamicList();

        assertEquals(1, dynamicList.getListItems().size());
        assertEquals("Birmingham", dynamicList.getListItems().get(0).getLabel());
        assertEquals("3344", dynamicList.getListItems().get(0).getCode());
    }

}
