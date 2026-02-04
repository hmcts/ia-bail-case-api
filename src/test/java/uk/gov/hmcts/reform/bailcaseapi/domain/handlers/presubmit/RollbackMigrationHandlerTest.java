package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.TTL;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

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
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RollbackMigrationHandlerTest {

    @Mock
    private Callback<BailCase> callback;

    @Mock
    private CaseDetails<BailCase> caseDetails;

    @Mock
    private BailCase bailCase;

    private RollbackMigrationHandler rollbackMigrationHandler;

    @BeforeEach
    void setUp() {
        rollbackMigrationHandler = new RollbackMigrationHandler();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getEvent()).thenReturn(Event.ROLLBACK_MIGRATION);
    }

    @Test
    void should_remove_ttl_field() {
        PreSubmitCallbackResponse<BailCase> response =
            rollbackMigrationHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isEqualTo(bailCase);
        verify(bailCase, times(1)).remove(TTL);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = rollbackMigrationHandler.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_SUBMIT && event == Event.ROLLBACK_MIGRATION) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        assertThatThrownBy(() -> rollbackMigrationHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        assertThatThrownBy(() -> rollbackMigrationHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> rollbackMigrationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> rollbackMigrationHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> rollbackMigrationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> rollbackMigrationHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}