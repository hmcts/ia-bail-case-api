package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.TTL;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.ccddataservice.TimeToLiveDataService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class RecordTheDecisionConfirmationTest {
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private TTL ttl;

    @Mock
    private TimeToLiveDataService timeToLiveDataService;

    private RecordTheDecisionConfirmation recordTheDecisionConfirmation;

    @BeforeEach
    void setup() {
        recordTheDecisionConfirmation = new RecordTheDecisionConfirmation(timeToLiveDataService);
    }

    @Test
    void should_handle_only_valid_event() {
        for (Event event: Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            boolean canHandle = recordTheDecisionConfirmation.canHandle(callback);
            if (event.equals(Event.RECORD_THE_DECISION)) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    void should_not_handle_invalid_event() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        assertThatThrownBy(() -> recordTheDecisionConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_set_ttl_not_suspended_if_necessary() {
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(BailCaseFieldDefinition.TTL, TTL.class)).thenReturn(Optional.of(ttl));
        when(ttl.getSuspended()).thenReturn(YesOrNo.YES);
        when(caseDetails.getState()).thenReturn(State.UNSIGNED_DECISION);

        PostSubmitCallbackResponse response = recordTheDecisionConfirmation
            .handle(callback);

        verify(timeToLiveDataService, times(1)).updateTheClock(callback, false);
    }

    @Test
    void should_not_set_ttl_suspended_property_if_already_unsuspended() {
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(BailCaseFieldDefinition.TTL, TTL.class)).thenReturn(Optional.of(ttl));
        when(ttl.getSuspended()).thenReturn(YesOrNo.NO); // suspended=NO (TTL already active)
        when(caseDetails.getState()).thenReturn(State.UNSIGNED_DECISION);

        PostSubmitCallbackResponse response = recordTheDecisionConfirmation
            .handle(callback);

        verify(timeToLiveDataService, never()).updateTheClock(callback, false);
    }

    @Test
    void should_not_manage_ttl_if_call_unsuccessful() {
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(caseDetails.getState()).thenReturn(State.APPLICATION_SUBMITTED); // Unsuccessful call: wrong state

        PostSubmitCallbackResponse response = recordTheDecisionConfirmation
            .handle(callback);

        verify(timeToLiveDataService, never()).updateTheClock(callback, false);
    }

}
