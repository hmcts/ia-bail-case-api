package uk.gov.hmcts.reform.bailcaseapi.infrastructure.ccddataservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.TTL;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.ccddataservice.TimeToLiveDataService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdDataApi;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class TimeToLiveDataServiceTest {

    @Mock
    private CcdDataApi ccdDataApi;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private IdamService idamService;
    @Mock
    private AuthTokenGenerator serviceAuthorization;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private TTL ttl;
    @Mock
    private StartEventDetails startEventDetails;
    @Mock
    private SubmitEventDetails submitEventDetails;

    private static final String USER_TOKEN = "userToken";
    private static final String S2S_TOKEN = "s2sToken";
    private static final String UID = "uid";
    private static final String CASE_TYPE = "Bail";
    private static final String JURISDICTION = "IA";
    private static final String EVENT_TOKEN = "eventToken";
    private static final long CASE_ID = 1;


    private TimeToLiveDataService timeToLiveDataService;

    @BeforeEach
    void setup() {
        timeToLiveDataService = new TimeToLiveDataService(featureToggler,
                                                          ccdDataApi, idamService, serviceAuthorization);
    }

    @ParameterizedTest
    @MethodSource("iaRetainDisposeFlagData")
    void should_update_the_clock(boolean iaRetainDisposeFlag) {

        when(featureToggler.getValue("ia-retain-dispose", false)).thenReturn(iaRetainDisposeFlag);

        if (iaRetainDisposeFlag) {
            when(idamService.getUserToken()).thenReturn(USER_TOKEN);
            when(serviceAuthorization.generate()).thenReturn(S2S_TOKEN);
            when(idamService.getSystemUserId(USER_TOKEN)).thenReturn(UID);

            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(bailCase);
            when(caseDetails.getId()).thenReturn(CASE_ID);
            when(bailCase.read(BailCaseFieldDefinition.TTL, TTL.class)).thenReturn(Optional.of(ttl));

            when(ccdDataApi.startEvent(
                USER_TOKEN,
                S2S_TOKEN,
                UID,
                JURISDICTION,
                CASE_TYPE,
                "1",
                Event.MANAGE_CASE_TTL.toString()
            ))
                .thenReturn(startEventDetails);

            when(startEventDetails.getToken()).thenReturn(EVENT_TOKEN);
            when(startEventDetails.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(bailCase);

            CaseDataContent submitRequest = new CaseDataContent(
                "1",
                Map.of("TTL", ttl),
                Map.of("id", Event.MANAGE_CASE_TTL.toString()),
                EVENT_TOKEN,
                true
            );

            ArgumentCaptor<CaseDataContent> caseDataContentArgumentCaptor =
                ArgumentCaptor.forClass(CaseDataContent.class);

            when(ccdDataApi.submitEvent(
                eq(USER_TOKEN),
                eq(S2S_TOKEN),
                eq("1"),
                caseDataContentArgumentCaptor.capture()
            )).thenReturn(submitEventDetails);

            timeToLiveDataService.updateTheClock(callback, true);
            assertEquals(submitRequest, caseDataContentArgumentCaptor.getValue());

            verify(ccdDataApi, times(1)).submitEvent(
                USER_TOKEN,
                S2S_TOKEN,
                "1",
                caseDataContentArgumentCaptor.getValue()
            );
            verify(ttl, times(1)).setSuspended(YesOrNo.YES);
        } else {
            //do nothing
        }
    }

    private static Stream<Arguments> iaRetainDisposeFlagData() {
        return Stream.of(
            Arguments.of(false),
            Arguments.of(true)
        );
    }
}
