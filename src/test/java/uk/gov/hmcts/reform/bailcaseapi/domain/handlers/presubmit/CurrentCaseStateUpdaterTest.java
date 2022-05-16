package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_JUDGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State.DECISION_CONDITIONAL_BAIL;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CurrentCaseStateUpdaterTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    private CurrentCaseStateUpdater currentCaseStateUpdater =
        new CurrentCaseStateUpdater();

    @Test
    void should_set_case_building_ready_for_submission_flag_to_yes() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        for (State state : State.values()) {

            when(caseDetails.getState()).thenReturn(state);

            PreSubmitCallbackResponse<BailCase> callbackResponse =
                currentCaseStateUpdater
                    .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(bailCase, callbackResponse.getData());

            verify(bailCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE, state);
            verify(bailCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER, state);
            verify(bailCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE, state);
            verify(bailCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, state);
            reset(bailCase);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> currentCaseStateUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = currentCaseStateUpdater.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> currentCaseStateUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> currentCaseStateUpdater.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> currentCaseStateUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> currentCaseStateUpdater.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_update_state_to_conditionalGrant_based_on_decision_type() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of("conditionalGrant"));

        PreSubmitCallbackResponse<BailCase> response = currentCaseStateUpdater
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        State state = DECISION_CONDITIONAL_BAIL;
        assertNotNull(response);
        assertEquals(bailCase, response.getData());

        verify(bailCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE, state);
        verify(bailCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER, state);
        verify(bailCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE, state);
        verify(bailCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, state);
        reset(bailCase);
    }
}
