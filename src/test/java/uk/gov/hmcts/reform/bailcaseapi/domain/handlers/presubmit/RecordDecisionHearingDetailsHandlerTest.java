package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_START_TIME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.JUDGE_DETAILS_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.BailCaseServiceResponseException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordDecisionHearingDetailsHandlerTest {

    private static final String HEARING_ID = "2000001";

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private IaHearingsApiService iaHearingsApiService;

    private final BailCase bailCase = new BailCase();
    private RecordDecisionHearingDetailsHandler handler;

    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(iaHearingsApiService.aboutToSubmit(callback)).thenAnswer(invocation -> {
            BailCase fromHearingsApi = new BailCase();
            fromHearingsApi.putAll(bailCase);
            return fromHearingsApi;
        });

        bailCase.write(JUDGE_DETAILS_NAME, "Judge Judy");
        bailCase.write(ATTENDEES_AND_DURATION_START_TIME, "10:00");

        handler = new RecordDecisionHearingDetailsHandler(iaHearingsApiService);
    }

    @Test
    void should_send_the_case_to_ia_hearings_api_then_drop_the_hearing_details() {
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_ID, HEARING_ID);

        PreSubmitCallbackResponse<BailCase> response = handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(iaHearingsApiService).aboutToSubmit(callback);
        assertEquals("Judge Judy", response.getData().read(JUDGE_DETAILS_NAME, String.class).orElseThrow());
        assertNull(response.getData().get(ATTENDEES_AND_DURATION_HEARING_ID.value()));
        assertNull(response.getData().get(ATTENDEES_AND_DURATION_START_TIME.value()));
    }

    @Test
    void should_send_the_case_when_only_the_hearing_list_selection_is_present() {
        Value selected = new Value(HEARING_ID, "Bail");
        bailCase.write(ATTENDEES_AND_DURATION_HEARING, new DynamicList(selected, List.of(selected)));

        PreSubmitCallbackResponse<BailCase> response = handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(iaHearingsApiService).aboutToSubmit(callback);
        assertNull(response.getData().get(ATTENDEES_AND_DURATION_HEARING.value()));
    }

    @Test
    void should_not_call_ia_hearings_api_when_no_hearing_was_selected() {
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_ID, " ");
        bailCase.write(
            ATTENDEES_AND_DURATION_HEARING,
            new DynamicList(new Value("", ""), List.of(new Value(HEARING_ID, "Bail")))
        );

        PreSubmitCallbackResponse<BailCase> response = handler.handle(ABOUT_TO_SUBMIT, callback);

        verifyNoInteractions(iaHearingsApiService);
        assertEquals(bailCase, response.getData());
        assertEquals("Judge Judy", response.getData().read(JUDGE_DETAILS_NAME, String.class).orElseThrow());
    }

    @Test
    void should_fail_the_event_when_ia_hearings_api_fails() {
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_ID, HEARING_ID);
        when(iaHearingsApiService.aboutToSubmit(callback))
            .thenThrow(new BailCaseServiceResponseException("failed", new RuntimeException()));

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(BailCaseServiceResponseException.class);
    }

    @Test
    void should_only_handle_about_to_submit_of_record_the_decision() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                boolean expected = event == Event.RECORD_THE_DECISION && stage == ABOUT_TO_SUBMIT;
                assertEquals(expected, handler.canHandle(stage, callback));
            }
        }

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> handler.canHandle(ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
