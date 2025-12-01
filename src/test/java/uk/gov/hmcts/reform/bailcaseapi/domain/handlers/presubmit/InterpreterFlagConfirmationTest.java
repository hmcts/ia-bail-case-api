package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.service.CcdDataService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class InterpreterFlagConfirmationTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private CcdDataService ccdDataService;
    private InterpreterFlagConfirmation interpreterFlagConfirmation;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        interpreterFlagConfirmation = new InterpreterFlagConfirmation(ccdDataService);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CREATE_FLAG", "MANAGE_FLAGS"})
    void should_handle_event(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertTrue(interpreterFlagConfirmation.canHandle(callback));
        interpreterFlagConfirmation.handle(callback);

        verify(ccdDataService, times(1)).setActiveInterpreterFlag(callback);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CREATE_FLAG", "MANAGE_FLAGS"}, mode = EnumSource.Mode.EXCLUDE)
    void should_not_handle_event(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertFalse(interpreterFlagConfirmation.canHandle(callback));
        assertThatThrownBy(() -> interpreterFlagConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_confirmation_body_error_message_if_failed_event() {
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        when(ccdDataService.setActiveInterpreterFlag(callback)).thenThrow((new RuntimeException("some error")));

        PostSubmitCallbackResponse response = interpreterFlagConfirmation.handle(callback);
        verify(ccdDataService, times(1)).setActiveInterpreterFlag(callback);
        assertTrue(response.getConfirmationBody().isPresent());
        assertEquals("### Something went wrong\n\n", response.getConfirmationBody().get());
    }

    @Test
    void should_not_confirmation_body_error_message_if_event_worked() {
        when(callback.getEvent()).thenReturn(Event.CREATE_FLAG);
        SubmitEventDetails submitEventDetails = new SubmitEventDetails();
        when(ccdDataService.setActiveInterpreterFlag(callback)).thenReturn(submitEventDetails);

        PostSubmitCallbackResponse response = interpreterFlagConfirmation.handle(callback);
        verify(ccdDataService, times(1)).setActiveInterpreterFlag(callback);
        assertTrue(response.getConfirmationBody().isEmpty());
    }
}

