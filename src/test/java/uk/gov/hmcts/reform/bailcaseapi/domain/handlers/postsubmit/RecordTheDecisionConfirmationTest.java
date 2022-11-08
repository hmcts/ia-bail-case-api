package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class RecordTheDecisionConfirmationTest {
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    private RecordTheDecisionConfirmation recordTheDecisionConfirmation;

    @BeforeEach
    void setup() {
        recordTheDecisionConfirmation = new RecordTheDecisionConfirmation();
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

}
