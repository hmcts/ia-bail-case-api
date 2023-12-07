package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class UpdateInterpreterBookingStatusHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    private UpdateInterpreterBookingStatusHandler updateInterpreterBookingStatusHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_BOOKING_STATUS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        updateInterpreterBookingStatusHandler = new UpdateInterpreterBookingStatusHandler();
    }

    @Test
    void should_clear_applicant_spoken_and_sign_booking_statuses_from_case_data() {

        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<BailCase> response = updateInterpreterBookingStatusHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        verify(bailCase, times(1)).clear(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS);
        verify(bailCase, times(1)).clear(APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS);
    }

    @Test
    void should_clear_fcs_spoken_and_sign_booking_statuses_from_case_data() {

        when(bailCase.read(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_1, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_2, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_3, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_4, String.class)).thenReturn(Optional.empty());

        when(bailCase.read(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_1, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_2, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_3, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_4, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<BailCase> response = updateInterpreterBookingStatusHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        verify(bailCase, times(1)).clear(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1);
        verify(bailCase, times(1)).clear(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2);
        verify(bailCase, times(1)).clear(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3);
        verify(bailCase, times(1)).clear(FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4);
        verify(bailCase, times(1)).clear(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1);
        verify(bailCase, times(1)).clear(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2);
        verify(bailCase, times(1)).clear(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3);
        verify(bailCase, times(1)).clear(FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4);
    }

}