package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.BailCaseCallbackApiDelegator;

@ExtendWith(MockitoExtension.class)
class IaHearingsApiServiceTest {

    private static final String ENDPOINT = "http://hearings-api";

    @Mock
    private BailCaseCallbackApiDelegator bailCaseCallbackApiDelegator;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private BailCase bailCase;

    private IaHearingsApiService iaHearingsApiService;

    @BeforeEach
    void setUp() {
        iaHearingsApiService = new IaHearingsApiService(
            bailCaseCallbackApiDelegator,
            ENDPOINT,
            "/bail/ccdAboutToStart",
            "/bail/ccdMidEvent",
            "/bail/ccdAboutToSubmit"
        );
    }

    @Test
    void should_delegate_about_to_start() {
        when(bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT + "/bail/ccdAboutToStart"))
            .thenReturn(bailCase);

        assertEquals(bailCase, iaHearingsApiService.aboutToStart(callback));
    }

    @Test
    void should_delegate_mid_event_with_the_page_id() {
        when(callback.getPageId()).thenReturn("selectHearingRecordAttendeesAndDuration");
        when(bailCaseCallbackApiDelegator.delegate(
            callback, ENDPOINT + "/bail/ccdMidEvent?pageId=selectHearingRecordAttendeesAndDuration"))
            .thenReturn(bailCase);

        assertEquals(bailCase, iaHearingsApiService.midEvent(callback));
    }

    @Test
    void should_delegate_about_to_submit() {
        when(bailCaseCallbackApiDelegator.delegate(callback, ENDPOINT + "/bail/ccdAboutToSubmit"))
            .thenReturn(bailCase);

        assertEquals(bailCase, iaHearingsApiService.aboutToSubmit(callback));
    }
}
