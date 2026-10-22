package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent.INITIAL_LISTING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent.RELISTING;
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
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CaseListingSubmitStateHandlerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private PreSubmitCallbackResponse<BailCase> callbackResponse;
    @Mock
    private BailCase bailCase;

    private CaseListingSubmitStateHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CaseListingSubmitStateHandler();

        when(callback.getEvent()).thenReturn(Event.CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
    }

    @Test
    void should_set_bail_summary_uploaded_for_initial_listing_of_a_submitted_application() {
        when(caseDetails.getState()).thenReturn(State.APPLICATION_SUBMITTED);
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(INITIAL_LISTING));

        PreSubmitCallbackResponse<BailCase> response = handler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(response.getState()).isEqualTo(State.BAIL_SUMMARY_UPLOADED);
        assertThat(response.getData()).isEqualTo(bailCase);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void should_leave_the_state_unchanged_for_a_relisting() {
        when(caseDetails.getState()).thenReturn(State.APPLICATION_SUBMITTED);
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(RELISTING));

        PreSubmitCallbackResponse<BailCase> response = handler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(response.getState()).isEqualTo(State.APPLICATION_SUBMITTED);
    }

    @Test
    void should_leave_the_state_unchanged_when_the_listing_event_is_absent() {
        when(caseDetails.getState()).thenReturn(State.APPLICATION_SUBMITTED);
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<BailCase> response = handler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(response.getState()).isEqualTo(State.APPLICATION_SUBMITTED);
    }

    @Test
    void should_leave_the_state_unchanged_when_the_listing_event_cannot_be_read() {
        when(caseDetails.getState()).thenReturn(State.APPLICATION_SUBMITTED);
        when(bailCase.read(LISTING_EVENT, ListingEvent.class))
            .thenThrow(new IllegalArgumentException("not a listing event"));

        PreSubmitCallbackResponse<BailCase> response = handler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse);

        assertThat(response.getState()).isEqualTo(State.APPLICATION_SUBMITTED);
    }

    @Test
    void should_leave_the_state_unchanged_when_the_application_is_not_submitted() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(INITIAL_LISTING));

        for (State state : State.values()) {
            if (state == State.APPLICATION_SUBMITTED) {
                continue;
            }

            when(caseDetails.getState()).thenReturn(state);

            PreSubmitCallbackResponse<BailCase> response = handler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse);

            assertThat(response.getState()).isEqualTo(state);
        }
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = handler.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_SUBMIT && event == Event.CASE_LISTING) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.CASE_LISTING);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback, callbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null, callback, callbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, null, callbackResponse))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
