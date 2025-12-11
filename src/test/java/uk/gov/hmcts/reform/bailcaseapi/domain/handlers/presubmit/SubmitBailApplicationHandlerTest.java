package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdSupplementaryUpdater;

class SubmitBailApplicationHandlerTest {

    @Mock
    private CcdSupplementaryUpdater ccdSupplementaryUpdater;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    private SubmitBailApplicationHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new SubmitBailApplicationHandler(ccdSupplementaryUpdater);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
    }

    @Test
    void canHandle_returns_true_for_submit_application_and_about_to_submit() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_returns_false_for_wrong_event() {
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_returns_false_for_wrong_stage() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @Test
    void handle_updates_supplementary_and_returns_response_when_successful() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        when(ccdSupplementaryUpdater.setHmctsServiceIdSupplementary(callback)).thenReturn(true);

        PreSubmitCallbackResponse<BailCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(bailCase, response.getData());
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void handle_adds_error_when_update_unsuccessful() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        when(ccdSupplementaryUpdater.setHmctsServiceIdSupplementary(callback)).thenReturn(false);

        PreSubmitCallbackResponse<BailCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(bailCase, response.getData());
        assertFalse(response.getErrors().isEmpty());
        assertTrue(response.getErrors().contains("Failed to update HMCTS Service ID supplementary data. Please try again."));
    }

    @Test
    void handle_throws_exception_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);

        assertThrows(IllegalStateException.class, () ->
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)
        );
    }
}
